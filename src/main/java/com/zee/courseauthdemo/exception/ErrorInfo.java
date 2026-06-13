package com.zee.courseauthdemo.exception;

import com.zee.courseauthdemo.datatype.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @dev : Ezekiel Eromosei
 * @date : 20 Feb, 2025
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorInfo {
    private MessageType messageType;
    private String errorCode;
    private String detailedDescription;
    private String[] placeholderValues;
}
