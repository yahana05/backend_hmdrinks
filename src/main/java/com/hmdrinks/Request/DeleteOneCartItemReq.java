package com.hmdrinks.Request;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeleteOneCartItemReq {
    @Min(value = 1, message = "UserId phải lớn hơn 0")
    private int userId;
    @Min(value = 1, message = "CartItemId phải lớn hơn 0")
    private int cartItemId;
}
