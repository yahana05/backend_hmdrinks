package com.hmdrinks.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class CRUDCartGroupViewResponse {
    private Integer cartGroupId;
    private Integer groupId;
    private Integer userId;
    private Integer memberId;
    private Double TotalPrice;
    private Integer TotalQuantity;
    List<CRUDCartItemGroupResponse> listCartItemGroup;
}
