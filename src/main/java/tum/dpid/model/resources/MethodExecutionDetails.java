package tum.dpid.model.resources;

/**
 * Data model to parse snapshot results from sampling by using VisualVM
 */
public class MethodExecutionDetails {
        private final String name;
        /* Time as ms*/
        private final Double totalTime;
        private final Double totalTimeCpu;
        private final Integer hits;

        public MethodExecutionDetails(String name, Double totalTime, Double totalTimeCpu, Integer hits) {
            this.name = name;
            this.totalTime = totalTime;
            this.totalTimeCpu = totalTimeCpu;
            this.hits = hits;
        }

        public String getName() {
            return name;
        }

        public Double getTotalTime() {
            return totalTime;
        }

        public Double getTotalTimeCpu() {
            return totalTimeCpu;
        }

        public int getHits() {
            return hits;
        }

        @Override
        public String toString() {
            return "MethodExecutionDetails{" +
                    "name='" + name + '\'' +
                    ", totalTime=" + totalTime +
                    ", totalTimeCpu=" + totalTimeCpu +
                    ", hits=" + hits +
                    '}';
        }

}
