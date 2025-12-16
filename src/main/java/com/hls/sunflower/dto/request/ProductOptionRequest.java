// ...new file...
package com.hls.sunflower.dto.request;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductOptionRequest {
    private String id;
    private String name;
    // List of size ids or names (backward compatible): prefer ids
    private List<String> sizeIds;
    private List<String> sizes;
}
