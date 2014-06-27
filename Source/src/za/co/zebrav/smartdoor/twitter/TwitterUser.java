package za.co.zebrav.smartdoor.twitter;

import com.google.gson.annotations.SerializedName;
/*
 * The following code is taken from: https://github.com/Rockncoder/TwitterTutorial
 * A few minor changes was made from the above repository.
 */
public class TwitterUser
{
	@SerializedName("screen_name")
	private String screenName;

	@SerializedName("name")
	private String name;

	@SerializedName("profile_image_url")
	private String profileImageUrl;

	public String getProfileImageUrl()
	{
		return profileImageUrl;
	}

	public String getScreenName()
	{
		return screenName;
	}

	public void setProfileImageUrl(String profileImageUrl)
	{
		this.profileImageUrl = profileImageUrl;
	}

	public void setScreenName(String screenName)
	{
		this.screenName = screenName;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}
}
