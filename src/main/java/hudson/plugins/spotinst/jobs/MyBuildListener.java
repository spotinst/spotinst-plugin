//package hudson.plugins.spotinst.jobs;
//
//import hudson.Extension;
//import hudson.model.AbstractBuild;
//import hudson.model.Node;
//import hudson.model.Result;
//import hudson.model.TaskListener;
//import hudson.model.listeners.RunListener;
//import hudson.plugins.spotinst.slave.SpotinstSlave;
//
//@Extension
//public class MyBuildListener extends RunListener<AbstractBuild> {
//
//    @Override
//    public void onCompleted(AbstractBuild build, TaskListener listener) {
//        super.onCompleted(build, listener);
//
//        if (build.getResult() == Result.FAILURE) {
//            // Build failed, check if instance alive.
//            // if not
//            Node node = build.getBuiltOn();
//
//            if(node instanceof SpotinstSlave){
//                SpotinstSlave spotinstSlave = (SpotinstSlave) node;
//                String instanceId = spotinstSlave.getInstanceId();
//
//                if(instanceId != null){
//
//                }
//            }
//        }
//    }
//}
