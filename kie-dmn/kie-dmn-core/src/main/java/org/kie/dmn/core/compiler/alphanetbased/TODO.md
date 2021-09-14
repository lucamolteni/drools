Istanziare rete network chiamando i metodi di creazione delle RETE durante la generazione
Far restituire Alpha Node e metodo Java
Creare nuova interfaccia per result che non dipenda da ObjectSink
Inline creazione constraint
Cambiare creazione output
Aggiungere il metodo di inizializzazione ad ANC
Parametrizzare ANC in modo da avere un costruttore senza setNetworkNodeReference
Disabilita generazione di propagateModifyObject
Spostare 

    private boolean evaluateAllTests(PropertyEvaluator propertyEvaluator, CompiledFEELUnaryTests instance, int index, String traceString) {
        return instance.getUnaryTests().stream().anyMatch(t -> {
            Object value = propertyEvaluator.getValue(index);
            Boolean result = t.apply(propertyEvaluator.getEvaluationContext(), value);
            if (logger.isTraceEnabled()) {
                logger.trace(traceString);
            }
            return result != null && result;
        });
    }

nella ANC invece che in ogni classe di test

Esempio creazione ANC

    private boolean setNetworkNode0(org.drools.core.common.NetworkNode node) {
        lambdaConstraint4 = alphaNetworkCreation.createConstraint("Age_62_6118", p -> evaluateAllTests(p, UnaryTestR1C1.getInstance(), 0, "trace"), null).getLambdaConstraint();

        resultCollectorAlphaSink11 = alphaNetworkCreation.resultCollector(0, "", context -> R1C1FeelExpression.getInstance().apply(context));
    }


l'output (In questo caso resultCollectorAlphaSink) non ha bisogno di InternalFactHandle factHandle, PropagationContext propagationContext, InternalWorkingMemory workingMemory