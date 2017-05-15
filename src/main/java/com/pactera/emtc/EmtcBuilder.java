package com.pactera.emtc;
import com.alibaba.fastjson.JSON;
import com.pactera.emtc.model.ResponseModel;
import com.pactera.emtc.vendor.*;
import com.pactera.emtc.vendor.http.HttpsHandler;
import hudson.Launcher;
import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import net.sf.json.JSONObject;
import org.apache.commons.httpclient.NameValuePair;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;


public class EmtcBuilder extends Notifier {

    private final String appUrl;

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public EmtcBuilder(String appurl) {
        this.appUrl = appurl;
    }

    /**
     * We'll use this from the {@code config.jelly}.
     * @return
     */
    public String getAppurl() {
        return appUrl;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) {
        //检查参数
        String key = getDescriptor().getAppkey();
        String secret = getDescriptor().getAppsecret();
        if (key == null || key.length() <= 0){
            listener.getLogger().println("appkey未填写，停止创建EMTC测试任务");
        }
        if (secret == null || secret.length() <= 0 ){
            listener.getLogger().println("appsecret未填写，停止创建EMTC测试任务");
        }

        FileHandler fileHandler = new FileHandler(appUrl);
        if(!fileHandler.isFile()){
            listener.getLogger().println("app路径：文件不存在");
        }else{
            String today = Common.getToday();

            //------------ftp上传路径---------------
            String path = "/"+Common.getToday();
            FtpHandler ftpHandler = new FtpHandler(FtpHandler.Enviroment.PRODUCE);
            ftpHandler.ftpUpload(fileHandler,path);

            //-------------http请求----------------
            listener.getLogger().println("正在创建EMTC测试任务......");

            //生产
            String host = "emtc.pactera.com";
            HttpsHandler httpsHandler = new HttpsHandler(host);
            //test
//            String host = "58.215.221.218";
//            HttpsHandler httpsHandler = new HttpsHandler(host,8888);
            String url = "/jenkins";
            //post参数
            NameValuePair appServerPath = new NameValuePair("appPath",today+"/"+fileHandler.getMd5Name());
            NameValuePair appkey = new NameValuePair("appkey",key);
            NameValuePair appsecret = new NameValuePair("appsecret",secret);
            NameValuePair[] pairs = {appServerPath,appkey,appsecret};
            //post请求
            String response = httpsHandler.post(url,pairs);
            //转javabean
            ResponseModel responseModel =  JSON.parseObject(response,ResponseModel.class);
            if(responseModel.status == 0){
                listener.getLogger().println("EMTC测试任务构建失败");
                listener.getLogger().println("  error message: "+responseModel.err+"!");
            }else{
                listener.getLogger().println("EMTC测试任务构建完毕");
                listener.getLogger().println("  查看测试结果：http://"+host+"/showorderform/"+responseModel.data);
            }
        }
        return true;
    }

    // Overridden for better type safety.
    // If your vendor doesn't really define any property on Descriptor,
    // you don't have to do this.
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }
    /**
     * Descriptor for {@link EmtcBuilder}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     *
     * <p>
     * See {@code src/main/resources/hudson/plugins/hello_world/HelloWorldBuilder/*.jelly}
     * for the actual HTML fragment for the configuration screen.
     */
    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        /**
         * To persist global configuration information,
         * simply store it in a field and call save().
         *
         * <p>
         * If you don't want fields to be persisted, use {@code transient}.
         */
        private String appkey;
        private String appsecret;

        /**
         * In order to load the persisted global configuration, you have to 
         * call load() in the constructor.
         */
        public DescriptorImpl() {
            load();
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types 
            return true;
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return "创建EMTC测试任务";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            // To persist global configuration information,
            // set that to properties and call save().
            appkey = formData.getString("appkey");
            appsecret = formData.getString("appsecret");
            // ^Can also use req.bindJSON(this, formData);
            //  (easier when there are many fields; need set* methods for this, like setUseFrench)
            save();
            return super.configure(req,formData);
        }

        public String getAppkey() {
            return appkey;
        }

        public String getAppsecret(){
            return appsecret;
        }
    }
}

