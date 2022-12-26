package com.udacity.catpoint.image.service;

import java.awt.image.BufferedImage;

/**
 * Interface to describe the necessary behaviors of your
 * dependencies to make them easier to Mock
 *
 */
public interface ImageService {
    boolean imageContainsCat(BufferedImage image, float confidenceThreshold);

}
