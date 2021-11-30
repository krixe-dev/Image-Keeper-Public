package com.keeper.image.manager.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.core.io.Resource;

/**
 * Wrapper class for combining image resource data and file name
 */
@Getter
@Setter
@AllArgsConstructor
public class ResourceWrapper {
    private Resource resource;
    private String originalFileName;
}
