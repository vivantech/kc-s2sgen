/*
 * Kuali Coeus, a comprehensive research administration system for higher education.
 * 
 * Copyright 2005-2015 Kuali, Inc.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kuali.coeus.s2sgen.impl.generate.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * This abstract class has methods that are common to all the versions of
 * RRKeyPersonExpanded form.
 * 
 * @author Kuali Research Administration Team (kualidev@oncourse.iu.edu)
 */
public abstract class RRKeyPersonExpandedBaseGenerator extends
		RRKeyPersonBase {


	protected static final int PROFILE_TYPE = 18;
	protected static final String CO_INVESTIGATOR = "COI";
	protected static final int MAX_KEY_PERSON_COUNT = 40;
	protected static final int DIRECTORY_TITLE_MAX_LENGTH = 45;
	protected static final int ROLE_DESCRIPTION_MAX_LENGTH = 40;

    protected String pIPersonOrRolodexId = null;

}
