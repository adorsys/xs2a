package de.adorsys.psd2;

import de.adorsys.psd2.model.AccountIdentificationCode;
import de.adorsys.psd2.model.AuthenticationType;
import de.adorsys.psd2.model.CashAccountType;
import de.adorsys.psd2.model.ClearingSystemIdentificationCode;
import de.adorsys.psd2.model.MessageCodeServiceUnspecific400;
import de.adorsys.psd2.model.MessageCodeServiceUnspecific401;
import de.adorsys.psd2.model.MessageCodeServiceUnspecific403;
import de.adorsys.psd2.model.MessageCodeServiceUnspecific404;
import de.adorsys.psd2.model.MessageCodeServiceUnspecific405;
import de.adorsys.psd2.model.MessageCodeServiceUnspecific409;
import de.adorsys.psd2.model.OrganisationIdentificationCode;
import de.adorsys.psd2.model.PartyIdentificationMatchCode;
import de.adorsys.psd2.model.PartyNameMatchCode;
import de.adorsys.psd2.model.PersonIdentificationCode;
import de.adorsys.psd2.model.ProxyAccountTypeCode;
import de.adorsys.psd2.model.StatusReasonCode;
import de.adorsys.psd2.model.TransactionStatus;
import de.adorsys.psd2.model.VOPBulkStatus;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;

/**
 * This class provides Spring Converter beans for the enum models in the OpenAPI specification.
 *
 * By default, Spring only converts primitive types to enums using Enum::valueOf, which can prevent
 * correct conversion if the OpenAPI specification is using an `enumPropertyNaming` other than
 * `original` or the specification has an integer enum.
 */
@Configuration(value = "de.adorsys.psd2.enumConverterConfiguration")
public class EnumConverterConfiguration {

    @Bean(name = "de.adorsys.psd2.EnumConverterConfiguration.accountIdentificationCodeConverter")
    Converter<String, AccountIdentificationCode> accountIdentificationCodeConverter() {
        return new Converter<String, AccountIdentificationCode>() {
            @Override
            public AccountIdentificationCode convert(String source) {
                return AccountIdentificationCode.fromValue(source);
            }
        };
    }
    @Bean(name = "de.adorsys.psd2.EnumConverterConfiguration.authenticationTypeConverter")
    Converter<String, AuthenticationType> authenticationTypeConverter() {
        return new Converter<String, AuthenticationType>() {
            @Override
            public AuthenticationType convert(String source) {
                return AuthenticationType.fromValue(source);
            }
        };
    }
    @Bean(name = "de.adorsys.psd2.EnumConverterConfiguration.cashAccountTypeConverter")
    Converter<String, CashAccountType> cashAccountTypeConverter() {
        return new Converter<String, CashAccountType>() {
            @Override
            public CashAccountType convert(String source) {
                return CashAccountType.fromValue(source);
            }
        };
    }
    @Bean(name = "de.adorsys.psd2.EnumConverterConfiguration.clearingSystemIdentificationCodeConverter")
    Converter<String, ClearingSystemIdentificationCode> clearingSystemIdentificationCodeConverter() {
        return new Converter<String, ClearingSystemIdentificationCode>() {
            @Override
            public ClearingSystemIdentificationCode convert(String source) {
                return ClearingSystemIdentificationCode.fromValue(source);
            }
        };
    }
    @Bean(name = "de.adorsys.psd2.EnumConverterConfiguration.messageCodeServiceUnspecific400Converter")
    Converter<String, MessageCodeServiceUnspecific400> messageCodeServiceUnspecific400Converter() {
        return new Converter<String, MessageCodeServiceUnspecific400>() {
            @Override
            public MessageCodeServiceUnspecific400 convert(String source) {
                return MessageCodeServiceUnspecific400.fromValue(source);
            }
        };
    }
    @Bean(name = "de.adorsys.psd2.EnumConverterConfiguration.messageCodeServiceUnspecific401Converter")
    Converter<String, MessageCodeServiceUnspecific401> messageCodeServiceUnspecific401Converter() {
        return new Converter<String, MessageCodeServiceUnspecific401>() {
            @Override
            public MessageCodeServiceUnspecific401 convert(String source) {
                return MessageCodeServiceUnspecific401.fromValue(source);
            }
        };
    }
    @Bean(name = "de.adorsys.psd2.EnumConverterConfiguration.messageCodeServiceUnspecific403Converter")
    Converter<String, MessageCodeServiceUnspecific403> messageCodeServiceUnspecific403Converter() {
        return new Converter<String, MessageCodeServiceUnspecific403>() {
            @Override
            public MessageCodeServiceUnspecific403 convert(String source) {
                return MessageCodeServiceUnspecific403.fromValue(source);
            }
        };
    }
    @Bean(name = "de.adorsys.psd2.EnumConverterConfiguration.messageCodeServiceUnspecific404Converter")
    Converter<String, MessageCodeServiceUnspecific404> messageCodeServiceUnspecific404Converter() {
        return new Converter<String, MessageCodeServiceUnspecific404>() {
            @Override
            public MessageCodeServiceUnspecific404 convert(String source) {
                return MessageCodeServiceUnspecific404.fromValue(source);
            }
        };
    }
    @Bean(name = "de.adorsys.psd2.EnumConverterConfiguration.messageCodeServiceUnspecific405Converter")
    Converter<String, MessageCodeServiceUnspecific405> messageCodeServiceUnspecific405Converter() {
        return new Converter<String, MessageCodeServiceUnspecific405>() {
            @Override
            public MessageCodeServiceUnspecific405 convert(String source) {
                return MessageCodeServiceUnspecific405.fromValue(source);
            }
        };
    }
    @Bean(name = "de.adorsys.psd2.EnumConverterConfiguration.messageCodeServiceUnspecific409Converter")
    Converter<String, MessageCodeServiceUnspecific409> messageCodeServiceUnspecific409Converter() {
        return new Converter<String, MessageCodeServiceUnspecific409>() {
            @Override
            public MessageCodeServiceUnspecific409 convert(String source) {
                return MessageCodeServiceUnspecific409.fromValue(source);
            }
        };
    }
    @Bean(name = "de.adorsys.psd2.EnumConverterConfiguration.organisationIdentificationCodeConverter")
    Converter<String, OrganisationIdentificationCode> organisationIdentificationCodeConverter() {
        return new Converter<String, OrganisationIdentificationCode>() {
            @Override
            public OrganisationIdentificationCode convert(String source) {
                return OrganisationIdentificationCode.fromValue(source);
            }
        };
    }
    @Bean(name = "de.adorsys.psd2.EnumConverterConfiguration.partyIdentificationMatchCodeConverter")
    Converter<String, PartyIdentificationMatchCode> partyIdentificationMatchCodeConverter() {
        return new Converter<String, PartyIdentificationMatchCode>() {
            @Override
            public PartyIdentificationMatchCode convert(String source) {
                return PartyIdentificationMatchCode.fromValue(source);
            }
        };
    }
    @Bean(name = "de.adorsys.psd2.EnumConverterConfiguration.partyNameMatchCodeConverter")
    Converter<String, PartyNameMatchCode> partyNameMatchCodeConverter() {
        return new Converter<String, PartyNameMatchCode>() {
            @Override
            public PartyNameMatchCode convert(String source) {
                return PartyNameMatchCode.fromValue(source);
            }
        };
    }
    @Bean(name = "de.adorsys.psd2.EnumConverterConfiguration.personIdentificationCodeConverter")
    Converter<String, PersonIdentificationCode> personIdentificationCodeConverter() {
        return new Converter<String, PersonIdentificationCode>() {
            @Override
            public PersonIdentificationCode convert(String source) {
                return PersonIdentificationCode.fromValue(source);
            }
        };
    }
    @Bean(name = "de.adorsys.psd2.EnumConverterConfiguration.proxyAccountTypeCodeConverter")
    Converter<String, ProxyAccountTypeCode> proxyAccountTypeCodeConverter() {
        return new Converter<String, ProxyAccountTypeCode>() {
            @Override
            public ProxyAccountTypeCode convert(String source) {
                return ProxyAccountTypeCode.fromValue(source);
            }
        };
    }
    @Bean(name = "de.adorsys.psd2.EnumConverterConfiguration.statusReasonCodeConverter")
    Converter<String, StatusReasonCode> statusReasonCodeConverter() {
        return new Converter<String, StatusReasonCode>() {
            @Override
            public StatusReasonCode convert(String source) {
                return StatusReasonCode.fromValue(source);
            }
        };
    }
    @Bean(name = "de.adorsys.psd2.EnumConverterConfiguration.transactionStatusConverter")
    Converter<String, TransactionStatus> transactionStatusConverter() {
        return new Converter<String, TransactionStatus>() {
            @Override
            public TransactionStatus convert(String source) {
                return TransactionStatus.fromValue(source);
            }
        };
    }
    @Bean(name = "de.adorsys.psd2.EnumConverterConfiguration.voPBulkStatusConverter")
    Converter<String, VOPBulkStatus> voPBulkStatusConverter() {
        return new Converter<String, VOPBulkStatus>() {
            @Override
            public VOPBulkStatus convert(String source) {
                return VOPBulkStatus.fromValue(source);
            }
        };
    }

}
