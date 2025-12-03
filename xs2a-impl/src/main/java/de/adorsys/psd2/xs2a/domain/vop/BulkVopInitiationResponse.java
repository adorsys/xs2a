package de.adorsys.psd2.xs2a.domain.vop;

import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * POJO class representing the response for bulk verification of party initiation.
 * This class holds the bulk VOP status and a list of party verification responses.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BulkVopInitiationResponse extends VopInitiationResponse{

    public BulkVopInitiationResponse(ErrorHolder errorHolder) {
        super(errorHolder);
    }
}
