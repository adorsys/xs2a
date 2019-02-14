package de.adorsys.psd2.xs2a.domain;

import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScaApproachHolder {
    private ScaApproach scaApproach;

    public boolean isNotEmpty() {
        return scaApproach != null;
    }
}
