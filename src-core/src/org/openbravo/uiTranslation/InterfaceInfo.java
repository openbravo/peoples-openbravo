package org.openbravo.uiTranslation;

public class InterfaceInfo {

	public static int TAB = 0, PROCESS = 1, FORM = 2;
	
	private String title = "";
	private String description = "";
	private String help = "";
	private String id = "";
	private String moduleId = "";
	private String moduleLanguage = "";
	private int interfaceType;
	
	public int getInterfaceType() { return interfaceType; }
	public void setInterfaceType(int interfaceType) { this.interfaceType = interfaceType; }
	
	public String getTitle() { return title; }
	public void setTitle(String title) { this.title = title; }
	
	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }
	
	public String getHelp() { return help; }
	public void setHelp(String help) { this.help = help; }
	
	public String getId() { return id; }
	public void setId(String id) { this.id = id; }
	
	public String getModuleId() { return moduleId; }
	public void setModuleId(String moduleId) { this.moduleId = moduleId; }
	
	public String getModuleLanguage() { return moduleLanguage; }
	public void setModuleLanguage(String moduleLanguage) { this.moduleLanguage = moduleLanguage; }
	
	
}
