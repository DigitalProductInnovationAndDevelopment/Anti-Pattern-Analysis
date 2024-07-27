package tum.dpid.model.resources;

public class MethodExecutionDetails {
        private String name;
        private double totalTime;
        private double totalTimeCpu;
        private int hits;

        public MethodExecutionDetails(String name, double totalTime, double totalTimeCpu, int hits) {
            this.name = name;
            this.totalTime = totalTime;
            this.totalTimeCpu = totalTimeCpu;
            this.hits = hits;
        }

        public String getName() {
            return name;
        }

        public double getTotalTime() {
            return totalTime;
        }

        public double getTotalTimeCpu() {
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
