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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.kuali.coeus.common.budget.api.nonpersonnel.BudgetLineItemContract;
import org.kuali.coeus.common.budget.api.period.BudgetPeriodContract;
import org.kuali.coeus.common.budget.api.personnel.BudgetPersonnelDetailsContract;
import org.kuali.coeus.s2sgen.api.core.S2SException;
import org.kuali.coeus.s2sgen.impl.budget.S2SCommonBudgetService;
import org.kuali.coeus.s2sgen.impl.datetime.S2SDateTimeService;
import org.kuali.coeus.s2sgen.impl.generate.S2SBaseFormGenerator;
import org.kuali.coeus.s2sgen.impl.budget.KeyPersonDto;
import org.kuali.coeus.propdev.api.budget.ProposalDevelopmentBudgetExtContract;
import org.kuali.coeus.propdev.api.core.ProposalDevelopmentDocumentContract;
import org.kuali.coeus.s2sgen.impl.budget.S2SBudgetCalculatorService;
import org.kuali.coeus.s2sgen.api.core.AuditError;
import org.kuali.coeus.s2sgen.impl.validate.S2SErrorHandlerService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.ArrayList;
import java.util.List;

/**
 * This abstract class has methods that are common to all the versions of RRFedNonFedBudget form.
 * 
 * @author Kuali Research Administration Team (kualidev@oncourse.iu.edu)
 */
public abstract class RRFedNonFedBudgetBaseGenerator extends S2SBaseFormGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(RRFedNonFedBudgetBaseGenerator.class);

    public static final String OTHERPERSONNEL_POSTDOC = "PostDoc";
    public static final String OTHERPERSONNEL_GRADUATE = "Grad";
    public static final String OTHERPERSONNEL_UNDERGRADUATE = "UnderGrad";
    public static final String OTHERPERSONNEL_SECRETARIAL = "Sec";
    public static final String OTHERCOST_DESCRIPTION = "Other";
    protected static final String NID_PD_PI = "PD/PI";
    public static final int ADDITIONAL_KEYPERSONS_ATTACHMENT = 11;
    public static final int BUDGET_JUSTIFICATION_ATTACHMENT = 131;
    public static final int ADDITIONAL_EQUIPMENT_ATTACHMENT = 12;
    
    protected static final int OTHERPERSONNEL_MAX_ALLOWED = 6;
    protected static final int ARRAY_LIMIT_IN_SCHEMA = 4;
    private static final String PARTICIPANT_COUNT_REQUIRED = "s2s.budget.participantcount.required";
    private static final String PARTICIPANT_COSTS_REQUIRED = "s2s.budget.participantcost.required";

    @Autowired
    @Qualifier("s2SBudgetCalculatorService")
    protected S2SBudgetCalculatorService s2sBudgetCalculatorService;

    @Autowired
    @Qualifier("s2SDateTimeService")
    protected S2SDateTimeService s2SDateTimeService;

    @Autowired
    @Qualifier("s2SErrorHandlerService")
    protected S2SErrorHandlerService s2SErrorHandlerService;

    @Autowired
    @Qualifier("s2SCommonBudgetService")
    protected S2SCommonBudgetService s2SCommonBudgetService;

    /**
     * This method check whether the key person has a personnel budget  
     * 
     * @param keyPerson
     *            (KeyPersonInfo) key person entry.
     * @param period
     *            budget period
     * @return true if key person has personnel budget else false.
     */
    protected Boolean hasPersonnelBudget(KeyPersonDto keyPerson,int period){
        ProposalDevelopmentBudgetExtContract budget = s2SCommonBudgetService.getBudget(pdDoc.getDevelopmentProposal());
        List<? extends BudgetLineItemContract> budgetLineItemList = budget.getBudgetPeriods().get(period - 1).getBudgetLineItems();
           
        for(BudgetLineItemContract budgetLineItem : budgetLineItemList) {
            for(BudgetPersonnelDetailsContract budgetPersonnelDetails : budgetLineItem.getBudgetPersonnelDetailsList()){
                if( budgetPersonnelDetails.getPersonId().equals(keyPerson.getPersonId())){
                    return true;
                } else if (keyPerson.getRolodexId() != null && budgetPersonnelDetails.getPersonId().equals(keyPerson.getRolodexId().toString())) {
                    return true;
                }
            }
        }
        return false;       
    }
    
    /**
     * Perform manual validations on the budget. Similarly done in RRBudgetBaseGenerator.
     * @param pdDoc
     * @return
     * @throws S2SException
     */
    protected boolean validateBudgetForForm(ProposalDevelopmentDocumentContract pdDoc) throws S2SException {
        boolean valid = true;

        ProposalDevelopmentBudgetExtContract budget = s2SCommonBudgetService.getBudget(pdDoc.getDevelopmentProposal());
        if(budget != null) {
            for (BudgetPeriodContract period : budget.getBudgetPeriods()) {
                List<String> participantSupportCode = new ArrayList<String>();
                participantSupportCode.add(s2sBudgetCalculatorService.getParticipantSupportCategoryCode());
                List<? extends BudgetLineItemContract> participantSupportLineItems =
                        s2sBudgetCalculatorService.getMatchingLineItems(period.getBudgetLineItems(), participantSupportCode);
                int numberOfParticipants = period.getNumberOfParticipants() == null ? 0 : period.getNumberOfParticipants();
                if (!participantSupportLineItems.isEmpty() && numberOfParticipants == 0) {
                    AuditError auditError= s2SErrorHandlerService.getError(PARTICIPANT_COUNT_REQUIRED, getFormName());
                    AuditError error= new AuditError(auditError.getErrorKey(),
                            auditError.getMessageKey()+period.getBudgetPeriod(),auditError.getLink());
                    getAuditErrors().add(error);
                    valid = false;
                } else if (numberOfParticipants > 0 && participantSupportLineItems.isEmpty()) {
                    getAuditErrors().add(s2SErrorHandlerService.getError(PARTICIPANT_COSTS_REQUIRED, getFormName()));
                    valid = false;
                }
            }
        }
        return valid;
    }

    public S2SBudgetCalculatorService getS2sBudgetCalculatorService() {
        return s2sBudgetCalculatorService;
    }

    public void setS2sBudgetCalculatorService(S2SBudgetCalculatorService s2sBudgetCalculatorService) {
        this.s2sBudgetCalculatorService = s2sBudgetCalculatorService;
    }

    public S2SDateTimeService getS2SDateTimeService() {
        return s2SDateTimeService;
    }

    public void setS2SDateTimeService(S2SDateTimeService s2SDateTimeService) {
        this.s2SDateTimeService = s2SDateTimeService;
    }

    public S2SErrorHandlerService getS2SErrorHandlerService() {
        return s2SErrorHandlerService;
    }

    public void setS2SErrorHandlerService(S2SErrorHandlerService s2SErrorHandlerService) {
        this.s2SErrorHandlerService = s2SErrorHandlerService;
    }

    public S2SCommonBudgetService getS2SCommonBudgetService() {
        return s2SCommonBudgetService;
    }

    public void setS2SCommonBudgetService(S2SCommonBudgetService s2SCommonBudgetService) {
        this.s2SCommonBudgetService = s2SCommonBudgetService;
    }
}
