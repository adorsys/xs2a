package de.adorsys.aspsp.xs2a.web.util;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(pattern = ApiDateConstants.DATE_PATTERN, timezone = ApiDateConstants.UTC)
public @interface JsonFormatDateUTC {
}
