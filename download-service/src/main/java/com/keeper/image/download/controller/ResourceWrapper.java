package com.keeper.image.download.controller;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.core.io.Resource;

/**
 * Wrapper class for handling image resource and file name
 */
@Getter
@Setter
@AllArgsConstructor
public class ResourceWrapper {
    private Resource resource;
    private String originalFileName;
}
