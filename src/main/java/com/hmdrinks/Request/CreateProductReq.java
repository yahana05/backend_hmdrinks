package com.hmdrinks.Request;


import com.hmdrinks.Enum.Language;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateProductReq {
    @Min(value = 1, message = "cateId phải lớn hơn 0")
    private int cateId;
    private String proName;
    private String proImg;
    private String description;
    Language language;
}
