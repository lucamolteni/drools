package org.drools.kiesession.agenda;

import java.util.Iterator;
import java.util.Map;

import org.drools.core.common.ActivationsFilter;
import org.drools.core.common.AgendaGroupsManager;
import org.drools.core.common.InternalActivationGroup;
import org.drools.core.common.InternalAgenda;
import org.drools.core.common.InternalAgendaGroup;
import org.drools.core.common.InternalWorkingMemory;
import org.drools.core.common.PropagationContext;
import org.drools.core.common.ReteEvaluator;
import org.drools.core.common.SimpleAgendaGroup;
import org.drools.core.concurrent.GroupEvaluator;
import org.drools.core.concurrent.ParallelGroupEvaluator;
import org.drools.core.concurrent.SequentialGroupEvaluator;
import org.drools.core.event.AgendaEventSupport;
import org.drools.core.phreak.ExecutableEntry;
import org.drools.core.phreak.PropagationEntry;
import org.drools.core.phreak.PropagationList;
import org.drools.core.phreak.RuleAgendaItem;
import org.drools.core.phreak.SimplePropagationList;
import org.drools.core.phreak.ThreadUnsafePropagationList;
import org.drools.core.reteoo.PathMemory;
import org.drools.core.reteoo.RuleTerminalNodeLeftTuple;
import org.drools.core.reteoo.RuntimeComponentFactory;
import org.drools.core.reteoo.TerminalNode;
import org.drools.core.rule.consequence.InternalMatch;
import org.drools.core.rule.consequence.KnowledgeHelper;
import org.kie.api.runtime.rule.ActivationGroup;
import org.kie.api.runtime.rule.AgendaFilter;
import org.kie.api.runtime.rule.AgendaGroup;
import org.kie.api.runtime.rule.RuleFlowGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleAgenda implements InternalAgenda {

    protected static final Logger log = LoggerFactory.getLogger(SimpleAgenda.class );

    private final PropagationList propagationList;
    private final InternalWorkingMemory workingMemory;

    private final GroupEvaluator groupEvaluator;
    private KnowledgeHelper knowledgeHelper;
    private InternalAgendaGroup agendaGroup;

    public SimpleAgenda(InternalWorkingMemory workingMemory) {
        this.propagationList = new SimplePropagationList(workingMemory );
        this.workingMemory = workingMemory;

        this.groupEvaluator =  new SequentialGroupEvaluator(this );
        this.knowledgeHelper = RuntimeComponentFactory.get().createKnowledgeHelper(workingMemory.getReteEvaluator());
        this.agendaGroup = new SimpleAgendaGroup();
    }

    @Override
    public ReteEvaluator getReteEvaluator() {
        return workingMemory;
    }

    @Override
    public AgendaGroupsManager getAgendaGroupsManager() {
        return null;
    }

    @Override
    public AgendaEventSupport getAgendaEventSupport() {
        return this.workingMemory.getAgendaEventSupport();
    }

    @Override
    public ActivationsFilter getActivationsFilter() {
        return null;
    }

    @Override
    public void addEagerRuleAgendaItem(RuleAgendaItem item) {

    }

    @Override
    public void removeEagerRuleAgendaItem(RuleAgendaItem item) {

    }

    @Override
    public void addQueryAgendaItem(RuleAgendaItem item) {

    }

    @Override
    public void removeQueryAgendaItem(RuleAgendaItem item) {

    }

    @Override
    public void registerExpiration(PropagationContext expirationContext) {

    }

    @Override
    public void clearAndCancelActivationGroup(InternalActivationGroup activationGroup) {

    }

    @Override
    public RuleAgendaItem createRuleAgendaItem(int salience, PathMemory pathMemory, TerminalNode rtn) {
        return new RuleAgendaItem(salience, pathMemory, rtn, false, agendaGroup);
    }

    @Override
    public InternalMatch createAgendaItem(RuleTerminalNodeLeftTuple rtnLeftTuple, int salience, PropagationContext context, RuleAgendaItem ruleAgendaItem, InternalAgendaGroup agendaGroup) {
        return null;
    }

    @Override
    public void cancelActivation(InternalMatch internalMatch) {

    }

    @Override
    public void addItemToActivationGroup(InternalMatch internalMatch) {

    }

    @Override
    public RuleAgendaItem peekNextRule() {
        return null;
    }

    @Override
    public void flushPropagations() {

    }

    @Override
    public boolean isFiring() {
        return false;
    }

    @Override
    public void evaluateEagerList() {

    }

    @Override
    public void evaluateQueriesForRule(RuleAgendaItem item) {

    }

    @Override
    public KnowledgeHelper getKnowledgeHelper() {
        return knowledgeHelper;
    }

    @Override
    public void resetKnowledgeHelper() {

    }

    @Override
    public void haltGroupEvaluation() {

    }

    @Override
    public void executeTask(ExecutableEntry executableEntry) {
        executableEntry.execute();
    }

    @Override
    public void addPropagation(PropagationEntry propagationEntry) {
        propagationList.addEntry(propagationEntry);
    }

    @Override
    public InternalWorkingMemory getWorkingMemory() {
        return null;
    }

    @Override
    public boolean setFocus(String name) {
        return false;
    }

    @Override
    public void activateRuleFlowGroup(String name) {

    }

    @Override
    public void activateRuleFlowGroup(String name, String processInstanceId, String nodeInstanceId) {

    }

    @Override
    public void clearAndCancel() {

    }

    @Override
    public void clearAndCancelAgendaGroup(String name) {

    }

    @Override
    public void clearAndCancelActivationGroup(String name) {

    }

    @Override
    public void clearAndCancelRuleFlowGroup(String name) {

    }

    @Override
    public String getFocusName() {
        return null;
    }

    @Override
    public boolean isDeclarativeAgenda() {
        return false;
    }

    @Override
    public int fireAllRules(AgendaFilter agendaFilter, int fireLimit) {


        // Group evaluator is yet another indirection we can remove
        int returnedFireCount = groupEvaluator.evaluateAndFire( agendaGroup, agendaFilter, 0, fireLimit );

        return returnedFireCount;
    }

    @Override
    public void halt() {

    }

    @Override
    public void fireUntilHalt() {

    }

    @Override
    public void fireUntilHalt(AgendaFilter agendaFilter) {

    }

    @Override
    public boolean dispose(InternalWorkingMemory wm) {
        propagationList.dispose();
        return true;
    }

    @Override
    public boolean isAlive() {
        return true;
    }

    @Override
    public void reset() {

    }

    @Override
    public void setActivationsFilter(ActivationsFilter filter) {

    }

    @Override
    public void executeFlush() {

    }

    @Override
    public void activate() {

    }

    @Override
    public void deactivate() {

    }

    @Override
    public boolean tryDeactivate() {
        return false;
    }

    @Override
    public Map<String, InternalActivationGroup> getActivationGroupsMap() {
        return null;
    }

    @Override
    public int sizeOfRuleFlowGroup(String s) {
        return 0;
    }

    @Override
    public boolean isRuleActiveInRuleFlowGroup(String ruleflowGroupName, String ruleName, String processInstanceId) {
        return false;
    }

    @Override
    public void notifyWaitOnRest() {

    }

    @Override
    public Iterator<PropagationEntry> getActionsIterator() {
        return null;
    }

    @Override
    public boolean hasPendingPropagations() {
        return false;
    }

    @Override
    public boolean isParallelAgenda() {
        return false;
    }

    @Override
    public void clear() {

    }

    @Override
    public AgendaGroup getAgendaGroup(String name) {
        return null;
    }

    @Override
    public ActivationGroup getActivationGroup(String name) {
        return null;
    }

    @Override
    public RuleFlowGroup getRuleFlowGroup(String name) {
        return null;
    }
}
