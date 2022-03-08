package operator.crossover.impl;

import common.STATUS;
import variable.Variable;
import variable.impl.Task;
import operator.crossover.CrossoverOperator;
import representation.Solution;
import utils.DataUtil;
import utils.NumberUtil;
import utils.TimeUtils;

import java.util.ArrayList;
import java.util.List;

public class ProposedCrossoverOperator1 extends CrossoverOperator {
    @Override
    public Object execute(Object object) {
        List<Solution> matingSolutions = (List<Solution>) object;
        List<Solution> crossoveredSolutions = new ArrayList<>();

        int solutionSetSize = (int) this.getParameters().get("solutionSetSize");
        int matingSolutionSize = matingSolutions.size();
        while (crossoveredSolutions.size() <= solutionSetSize) {
            int parentIndex1 = (int) Math.floor(Math.random() * matingSolutionSize);
            int parentIndex2 = (int) Math.floor(Math.random() * matingSolutionSize);

            if(parentIndex1 != parentIndex2) {
                Solution parent1 = matingSolutions.get(parentIndex1);
                Solution parent2 = matingSolutions.get(parentIndex2);
                crossoveredSolutions.addAll(crossover(parent1, parent2));
            }
        }
        return crossoveredSolutions;
    }

    public List<Solution> crossover(Solution a, Solution b) {
        List<Solution> returnSolutions = new ArrayList<>();

        int size = a.getVariables().size();
        int chromosomeSize = a.getVariables().size();

        // Position trong gene ~ thu tu sap xep order trong list
        int position1 = 0;
        int position2 = 0;
        while (position1 >= position2) {
            position1 = NumberUtil.getRandomIntNumber(0, chromosomeSize);
            position2 = NumberUtil.getRandomIntNumber(0, chromosomeSize);
        }

        int variablePosition = NumberUtil.getRandomIntNumber(0, size);
        Solution copyA = DataUtil.cloneBean(a);
        Solution copyB = DataUtil.cloneBean(b);

        // get the crossover variables
        List<Variable> variablesA = copyA.getVariables();
        List<Variable> variablesB = copyB.getVariables();

        List<Variable> crossoverA = new ArrayList<>();
        List<Variable> crossoverB = new ArrayList<>();

        for (Variable variable: variablesA.subList(position1, position2)) {
            crossoverA.add(DataUtil.cloneBean(variable));
        }

        for (Variable variable: variablesB.subList(position1, position2)) {
            crossoverB.add(DataUtil.cloneBean(variable));
        }


        int j = crossoverA.size() - 1;
        for (int i = position1; i < position2; i++) {
            variablesA.remove(i);
            variablesA.add(i, crossoverB.get(j));

            variablesB.remove(i);
            variablesB.add(i, crossoverA.get(j));
            j--;
        }

        returnSolutions.add(copyA);
        returnSolutions.add(copyB);
        return returnSolutions;
    }
}
