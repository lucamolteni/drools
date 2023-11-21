package org.drools.core.common;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Map;

import org.drools.core.phreak.RuleAgendaItem;

public class SimpleAgendaGroup implements InternalAgendaGroup{

    Deque<RuleAgendaItem> agendaItems = new ArrayDeque<>();

    @Override
    public int size() {
        return agendaItems.size();
    }

    @Override
    public boolean isEmpty() {
        return agendaItems.isEmpty();
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public void setAutoFocusActivator(PropagationContext ctx) {

    }

    @Override
    public PropagationContext getAutoFocusActivator() {
        return null;
    }

    @Override
    public void setAutoDeactivate(boolean autoDeactivate) {

    }

    @Override
    public boolean isAutoDeactivate() {
        return false;
    }

    @Override
    public void reset() {

    }

    @Override
    public void add(RuleAgendaItem activation) {
        agendaItems.add(activation);
    }

    @Override
    public RuleAgendaItem peek() {
        return agendaItems.peek();
    }

    @Override
    public RuleAgendaItem remove() {
        return agendaItems.remove();
    }

    @Override
    public void remove(RuleAgendaItem activation) {
        agendaItems.remove(activation);
    }

    @Override
    public void setActive(boolean activate) {

    }

    @Override
    public void setActivatedForRecency(long recency) {

    }

    @Override
    public long getActivatedForRecency() {
        return 0;
    }

    @Override
    public void setClearedForRecency(long recency) {

    }

    @Override
    public long getClearedForRecency() {
        return 0;
    }

    @Override
    public void addNodeInstance(Object processInstanceId, String nodeInstanceId) {

    }

    @Override
    public void removeNodeInstance(Object processInstanceId, String nodeInstanceId) {

    }

    @Override
    public Collection<RuleAgendaItem> getActivations() {
        return null;
    }

    @Override
    public Map<Object, String> getNodeInstances() {
        return null;
    }

    @Override
    public void visited() {

    }

    @Override
    public void setReteEvaluator(ReteEvaluator reteEvaluator) {

    }

    @Override
    public void hasRuleFlowListener(boolean hasRuleFlowLister) {

    }

    @Override
    public boolean isRuleFlowListener() {
        return false;
    }

    @Override
    public boolean isSequential() {
        return false;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void clear() {

    }

    @Override
    public void setFocus() {

    }
}
