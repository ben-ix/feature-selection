import java.util.HashSet;
        import java.util.Set;

/**
 * Created by ben on 8/04/17.
 */
public class SequentialFloatingForwardSelection extends FeatureSelection {

    public SequentialFloatingForwardSelection(Set<Instance> instances){
        super(instances);
    }

    public Set<Integer> select(int maxNumFeatures) {
        return select((accuracy, size) -> size < maxNumFeatures);
    }

    public Set<Integer> select() {
        return select((noImprovement, size) -> noImprovement < MAX_ITERATIONS_WITHOUT_PROGRESS);
    }

    public Set<Integer> select(Criteria criteria) {
        // In this case we have no data to use, so return the empty set
        if (instances == null || instances.isEmpty()) return new HashSet<Integer>();

        // To begin with no features are selected, so all the indices from 0..totalFeatures are remaining
        Set<Integer> remainingFeatures = getAllFeatureIndices();

        // Subset of only selected features indices
        Set<Integer> selectedFeatures = new HashSet<>();

        Classifier classifier = new Classifier(instances);

        // Keep track of the best solution, so we never get worse
        double highestAccuracy = 0;
        Set<Integer> bestSoFar = new HashSet<>();
        double accuracy = objectiveFunction(selectedFeatures);
        double lastAccuracy = accuracy;

        // Number of iterations with no improvement
        double noImprovement = 0;

        while (criteria.evaluate(accuracy, selectedFeatures.size())){
            int feature = best(selectedFeatures, remainingFeatures);

            System.out.println("Selected features are:" + selectedFeatures);
            System.out.println("Remaining features are:" + remainingFeatures);
            System.out.println("Adding feature: " + feature);

            // No more valid features
            if (feature == -1) break;

            selectedFeatures.add(feature);
            // Remove the feature so we do not keep selecting the same one
            remainingFeatures.remove(feature);

            double accuracyBeforeRemoval = objectiveFunction(selectedFeatures);


            // Now remove the worst features, while we are improving
            while(true){
                int worstFeature = worst(selectedFeatures);

                // No more valid features
                if (worstFeature == -1) break;

                selectedFeatures.remove(worstFeature);
                // Feature becomes available again
                remainingFeatures.add(worstFeature);

                System.out.println("Removing " + worstFeature + " and adding back to remaining features");

                double newAccuracy = objectiveFunction(selectedFeatures);

                // If the accuracy did not improve, undo this step and continue adding features
                if (newAccuracy < accuracyBeforeRemoval) {
                    selectedFeatures.add(worstFeature);
                    remainingFeatures.remove(worstFeature);
                    System.out.println("Accuracy did not improve, so undoing above step");
                    break;
                }

                accuracyBeforeRemoval = newAccuracy;
            }

            accuracy = objectiveFunction(selectedFeatures);

            if (accuracy > highestAccuracy){
                highestAccuracy = accuracy;
                // Make a copy, so we don't accidentally modify this
                bestSoFar = new HashSet<>(selectedFeatures);
            }

            if (Double.compare(accuracy, lastAccuracy) <= 0){
                noImprovement++;
            } else{
                noImprovement = 0;
            }
            lastAccuracy = accuracy;

        }

        return bestSoFar;
    }


}