/*******************************************************************************
 * Copyright (c) 2010-2012, Zoltan Ujhelyi, Istvan Rath and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Zoltan Ujhelyi - initial API and implementation
 *******************************************************************************/
package org.eclipse.incquery.patternlanguage.formatting;

import org.eclipse.xtext.formatting.impl.AbstractDeclarativeFormatter;
import org.eclipse.xtext.formatting.impl.FormattingConfig;

/**
 * This class contains custom formatting description.
 */
public class PatternLanguageFormatter extends AbstractDeclarativeFormatter {

    @Override
    protected void configureFormatting(FormattingConfig c) {
        // It's usually a good idea to activate the following three statements.
        // They will add and preserve newlines around comments
        // c.setLinewrap(0, 1, 2).before(getGrammarAccess().getSL_COMMENTRule());
        // c.setLinewrap(0, 1, 2).before(getGrammarAccess().getML_COMMENTRule());
        // c.setLinewrap(0, 1, 1).after(getGrammarAccess().getML_COMMENTRule());
    }
}
