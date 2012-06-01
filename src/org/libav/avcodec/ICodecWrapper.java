/*
 * Copyright (C) 2012 Ondrej Perutka
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public 
 * License as published by the Free Software Foundation, either 
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library. If not, see 
 * <http://www.gnu.org/licenses/>.
 */
package org.libav.avcodec;

import org.libav.bridge.IWrapper;

/**
 * Wrapper for the AVCodec. It provides access to some of the AVCodec 
 * properties.
 * 
 * @author Ondrej Perutka
 */
public interface ICodecWrapper extends IWrapper {
    
    /**
     * Get the id property from the AVCodec.
     * 
     * WARNING:
     * The returned value may be cached. Call the clearWrapperCahce() if you
     * think the value have been changed.
     * 
     * @return 
     */
    int getId();
    
    /**
     * Get the type property from the AVCodec.
     * 
     * WARNING:
     * The returned value may be cached. Call the clearWrapperCahce() if you
     * think the value have been changed.
     * 
     * @return 
     */
    int getType();
    
    /**
     * Get the name property from the AVCodec.
     * 
     * WARNING:
     * The returned value may be cached. Call the clearWrapperCahce() if you
     * think the value have been changed.
     * 
     * @return 
     */
    String getName();
    
    /**
     * Get the capabilities property from the AVCodec.
     * 
     * WARNING:
     * The returned value may be cached. Call the clearWrapperCahce() if you
     * think the value have been changed.
     * 
     * @return codec capabilities
     */
    int getCapabilities();
    
}
