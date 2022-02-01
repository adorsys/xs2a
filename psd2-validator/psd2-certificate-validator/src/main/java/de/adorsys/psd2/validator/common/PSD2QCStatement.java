/*
 * Copyright 2018-2022 adorsys GmbH & Co KG
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 *
 * This project is also available under a separate commercial license. You can
 * contact us at psd2@adorsys.com.
 */

package de.adorsys.psd2.validator.common;

import de.adorsys.psd2.validator.certificate.CertificateErrorMsgCode;
import lombok.extern.slf4j.Slf4j;
import no.difi.certvalidator.api.CertificateValidationException;
import org.apache.commons.collections4.iterators.FilterIterator;
import org.bouncycastle.asn1.*;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.qualified.QCStatement;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.Optional;

@Slf4j
public class PSD2QCStatement {
    private static final ASN1ObjectIdentifier idEtsiPsd2QcStatement = new ASN1ObjectIdentifier("0.4.0.19495.2");

    private PSD2QCStatement() {
    }

	public static QCStatement psd2QCStatement() {
		return new QCStatement(idEtsiPsd2QcStatement);
	}

	public static PSD2QCType psd2QCType(X509Certificate cert) throws CertificateValidationException {
		byte[] extValues = cert.getExtensionValue(Extension.qCStatements.getId());
		if (extValues == null) {
			log.debug("QCStatement not found in psd2 certificate. Missing extension with value {}",
					Extension.qCStatements.getId());
			throw new CertificateValidationException(CertificateErrorMsgCode.CERTIFICATE_INVALID.toString());
		}

		QCStatement qcStatement = readQCStatement(extValues);

		ASN1Encodable statementInfo = qcStatement.getStatementInfo();

		return PSD2QCType.getInstance(statementInfo);

	}

	private static QCStatement readQCStatement(byte[] extensionValue) throws CertificateValidationException {

		ASN1Sequence qcStatements;
		try {
            try (ASN1InputStream derAsn1InputStream = new ASN1InputStream(new ByteArrayInputStream(extensionValue))) {
                DEROctetString oct = (DEROctetString) (derAsn1InputStream.readObject());
                try (ASN1InputStream asn1InputStream = new ASN1InputStream(oct.getOctets())) {
                    qcStatements = (ASN1Sequence) asn1InputStream.readObject();
                }
            }
		} catch (IOException e) {
			log.debug("Error reading qcstatement " + e);
			throw new CertificateValidationException(CertificateErrorMsgCode.CERTIFICATE_INVALID.toString());
		}

        if (qcStatements.size() <= 0) {
            log.debug("No ETSI PSD2 QcStatement in psd2 certificate");
            throw new CertificateValidationException(CertificateErrorMsgCode.CERTIFICATE_INVALID.toString());
        }

        ASN1Encodable object = qcStatements.getObjectAt(0);
        if (object.toASN1Primitive() instanceof ASN1ObjectIdentifier) {
            return getSingleQcStatement(qcStatements);
        }

        return getEtsiPsd2QcStatement(qcStatements)
            .orElseThrow(() -> new CertificateValidationException(CertificateErrorMsgCode.CERTIFICATE_INVALID.toString()));
    }

    /**
     * Iterate the list of qcStatements and try to find the EtsiPsd2 statement
     *
     * @param qcStatements input parameter to get the EtsiPsd2
     * @return Etsi Pds2 Statement or Optional.empty if not found
     */
    private static Optional<QCStatement> getEtsiPsd2QcStatement(ASN1Sequence qcStatements) {
        FilterIterator<ASN1Encodable> filteredIterator = new FilterIterator<>(qcStatements.iterator(), item -> {
            QCStatement qcStatement = QCStatement.getInstance(item);
            return qcStatement.getStatementId().getId().equals(idEtsiPsd2QcStatement.getId());
        });

        if (!filteredIterator.hasNext()) {
            log.debug("No ETSI PSD2 QcStatement in psd2 certificate");
            return Optional.empty();
        }

        return Optional.of(QCStatement.getInstance(filteredIterator.next()));
    }

    /**
     * This is a fallback for the cases where a single qcStatement with PSD2 items is added without wrapping it
     * in a SEQUENCE.
     *
     * @param qcStatements input parameter to get a single QC statement.
     * @return QCStatement
     * @throws CertificateValidationException in case of wrong statement type in psd2 certificate
     */
    private static QCStatement getSingleQcStatement(ASN1Sequence qcStatements) throws CertificateValidationException {
        // We have a single entity with oid and value direct
        QCStatement qcStatement = QCStatement.getInstance(qcStatements);
        if (!idEtsiPsd2QcStatement.getId().equals(qcStatement.getStatementId().getId())) {
            log.debug("Wrong statement type in psd2 certificate. expected is {} but found {}",
                idEtsiPsd2QcStatement.getId(), qcStatement.getStatementId().getId());
            throw new CertificateValidationException(CertificateErrorMsgCode.CERTIFICATE_INVALID.toString());
        }

        return qcStatement;
    }
}
