// ...new file...
package com.hls.sunflower.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductOptionResponse {
    private String id;
    private String name;
    private List<String> sizeIds;
    private List<String> sizeNames;
}
