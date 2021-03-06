package org.jenkinsci.plugins.deploygate;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;

import java.io.IOException;

import javax.servlet.ServletException;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

public class DeployGateNotifier extends Publisher {

	private String name;

	@Override
	public boolean perform(AbstractBuild build, Launcher launcher,
			BuildListener listener) {
		String UrlString = "https://deploygate.com/api/users/"
				+ getDescriptor().getUseOwnerName() + "/apps";

		DeployGateUploader uploder = new DeployGateUploader(UrlString,
				"test send", getDescriptor().getUseApiKey(), name);
		uploder.upload();
		listener.getLogger().println("uploder setting " + uploder);
		return true;
	}

	public BuildStepMonitor getRequiredMonitorService() {
		// TODO Auto-generated method stub
		return BuildStepMonitor.NONE;
	}

	@DataBoundConstructor
	public DeployGateNotifier(String name) {
		this.name = name;
	}

	@Override
	public DescriptorImpl getDescriptor() {
		return (DescriptorImpl) super.getDescriptor();
	}

	/**
	 * We'll use this from the <tt>config.jelly</tt>.
	 */
	public String getName() {
		return name;
	}

	@Extension
	// This indicates to Jenkins that this is an implementation of an extension
	// point.
	public static final class DescriptorImpl extends
			BuildStepDescriptor<Publisher> {
		/**
		 * To persist global configuration information, simply store it in a
		 * field and call save().
		 * 
		 * <p>
		 * If you don't want fields to be persisted, use <tt>transient</tt>.
		 */
		private String useOwnerName;
		private String useApiKey;

		/**
		 * Performs on-the-fly validation of the form field 'name'.
		 * 
		 * @param value
		 *            This parameter receives the value that the user has typed.
		 * @return Indicates the outcome of the validation. This is sent to the
		 *         browser.
		 */
		public FormValidation doCheckName(@QueryParameter String value)
				throws IOException, ServletException {
			if (value.length() == 0)
				return FormValidation.error("Please set a name");
			if (value.length() < 4)
				return FormValidation.warning("Isn't the name too short?");
			return FormValidation.ok();
		}

		/**
		 * This human readable name is used in the configuration screen.
		 */
		public String getDisplayName() {
			return "DeployGatePlugin";
		}

		@Override
		public boolean configure(StaplerRequest req, JSONObject formData)
				throws FormException {
			// To persist global configuration information,
			// set that to properties and call save().
			useOwnerName = formData.getString("ownerName");
			useApiKey = formData.getString("apiKey");
			// ^Can also use req.bindJSON(this, formData);
			// (easier when there are many fields; need set* methods for this,
			// like setUseFrench)
			save();
			return super.configure(req, formData);
		}

		public String getUseOwnerName() {
			return useOwnerName;
		}

		public String getUseApiKey() {
			return useApiKey;
		}

		@Override
		public boolean isApplicable(Class<? extends AbstractProject> jobType) {
			return true;
		}

	}
}
