/*
 * Copyright (c) 2007-2009, Osmorc Development Team
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright notice, this list
 *       of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice, this
 *       list of conditions and the following disclaimer in the documentation and/or other
 *       materials provided with the distribution.
 *     * Neither the name of 'Osmorc Development Team' nor the names of its contributors may be
 *       used to endorse or promote products derived from this software without specific
 *       prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.osmorc.frameworkintegration.impl.concierge;

import org.jetbrains.annotations.NotNull;
import org.osmorc.frameworkintegration.FrameworkRunner;
import org.osmorc.frameworkintegration.impl.AbstractFrameworkIntegrator;
import org.osmorc.frameworkintegration.impl.CommonRunPropertiesEditor;
import org.osmorc.run.ui.FrameworkRunPropertiesEditor;

/**
 * Concierge specific implementation of {@link org.osmorc.frameworkintegration.FrameworkIntegrator}.
 *
 * @author <a href="mailto:al@chilibi.org">Alain Greppin</a>
 */
public final class ConciergeIntegrator extends AbstractFrameworkIntegrator {
  private static final String FRAMEWORK_NAME = "Concierge";

  public ConciergeIntegrator() {
    super(new ConciergeInstanceManager());
  }

  @NotNull
  @Override
  public String getDisplayName() {
    return FRAMEWORK_NAME;
  }

  @NotNull
  @Override
  public FrameworkRunner createFrameworkRunner() {
    return new ConciergeRunner();
  }

  @Override
  public FrameworkRunPropertiesEditor createRunPropertiesEditor() {
    return new CommonRunPropertiesEditor();
  }
}
