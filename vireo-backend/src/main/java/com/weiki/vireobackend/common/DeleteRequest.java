package com.weiki.vireobackend.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用删除请求，只携带被删除记录的 id。
 */
@Data
public class DeleteRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    private static final long serialVersionUID = 1L;
}
