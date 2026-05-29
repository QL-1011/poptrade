package com.poptrade.dto;

import com.poptrade.common.page.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ChatSessionQueryDTO extends PageRequest {

    private String keyword;
}
