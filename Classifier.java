import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * Classifier to use as the evaluation criteria
 * for feature selection. K-Nearest neigbhour is
 * implemented here.
 */
public class Classifier {

    // How many neighbors to find
    private final int K = 3;

    Set<Instance> training = new HashSet<Instance>();
    Set<Instance> testing = new HashSet<Instance>();

    public Classifier(Set<Instance> instances) {
        // 70:30 training testing split
        int trainingSize = (int)(instances.size()*0.7);

        int count = 0;
        for(Instance instance: instances){
            if(count++ < trainingSize) training.add(instance);
            else testing.add(instance);
        }
    }

    public Classifier(Set<Instance> training, Set<Instance> testing){
        this.training = training;
        this.testing = testing;
    }

    /**
     * Classifies and returns the percentage
     * of correct classifications using every feature
     * in the instances.
     *
     */
    public double classify(){
        Instance sampleInstance = training.iterator().next();
        int totalFeatures = sampleInstance.getNumFeatures();

        // We are using all features
        Set<Integer> allIndices =  IntStream.rangeClosed(0, totalFeatures - 1)
                .boxed().collect(Collectors.toSet());

        return classify(allIndices);
    }
    /**
     * Classifies and returns the percentage
     * of correct classifications using only the specified indices
     * for the instances.
     */
    public double classify(Set<Integer> indices) {
        int correct = 0;

        for (Instance instance : testing) {

            //Keep neighbours in queue, to get the K closest neighbours quickly
            PriorityQueue<Result> neighbours = new PriorityQueue<Result>(new Comparator<Result>() {
                @Override
                public int compare(Result a, Result b) {
                    return a.DISTANCE < b.DISTANCE ? -1 : a.DISTANCE > b.DISTANCE ? 1 : 0;
                }
            });

            // Find the distance to every neighbor in training set
            for (Instance other : training) {
                Result result = new Result(other.getLabel(), instance.distanceTo(other, indices));
                neighbours.add(result);
            }

            //Take the K closest neighbours
            Result[] closestNeighbours = new Result[K];
            for(int i=0; i < K; i++){
                closestNeighbours[i] = neighbours.poll();
            }

            // Find the most common class among these K neighbours
            String type = mostCommonType(closestNeighbours);
            if(type.equals(instance.getLabel())){
                correct++;
            }
        }

        return correct/(double)testing.size();
    }


    /**
     * Returns the mode of @param list
     * @param list
     * @return
     */
    private String mostCommonType(Result[] list) {
        HashMap<String, Integer> occurences = new HashMap<String, Integer>();

        int maxFrequency = 0;
        String mostFrequent = null;

        for (Result r : list) {
            String type = r.CLASS;

            // If we have not seen this type yet, add it
            if (!occurences.containsKey(type)) {
                occurences.put(type, 0);
            }

            // Increment the frequency by 1
            int frequency = occurences.get(type) + 1;
            occurences.put(type, frequency);

            // Check if we have a new most frequent type
            if (frequency > maxFrequency) {
                mostFrequent = type;
                maxFrequency = frequency;
            }

        }


        return mostFrequent;

    }

    /**
     * Wrapper class to hold the outcomes
     * of K-NN.
     */
    private class Result {

        public final String CLASS;
        public final double DISTANCE;

        public Result(String type, double distance) {
            this.CLASS = type;
            this.DISTANCE = distance;
        }

        @Override
        public String toString() {
            return DISTANCE + " " + CLASS;
        }
    }

}
