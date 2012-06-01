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
package org.libav.net.rtsp;

/**
 * Abstract single media stream. Implements common single media stream methods.
 * 
 * @author Ondrej Perutka
 */
public abstract class AbstractSingleMediaStream extends AbstractMediaStream implements ISingleMediaStream {
    
    protected IMediaStream parent;

    public AbstractSingleMediaStream() {
        this.parent = null;
    }

    @Override
    public IMediaStream getParentStream() {
        return parent;
    }

    @Override
    public void setParentStream(IMediaStream parent) {
        this.parent = parent;
    }

    @Override
    public boolean isStandalone() {
        return parent == null;
    }
    
}
