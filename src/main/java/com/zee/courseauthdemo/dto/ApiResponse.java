package com.zee.courseauthdemo.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.zee.courseauthdemo.datatype.MessageType;
import com.zee.courseauthdemo.util.IgnoreForProdProfile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @dev : Ezekiel Eromosei
 * @date : 13 Jun, 2026
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    @Builder.Default
    private boolean successful = true;
    private MessageType messageType;
    private String errorCode;
    private String errorMessage;
    private List<AuthFieldError> fieldErrors;
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = IgnoreForProdProfile.class)
    private String errorDetailedDescription;
    private T data;
}
