package io.weline.repo.files.data;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

import io.weline.repo.data.model.PermissionsModel;

@Keep
public class UserInfo implements Serializable {

    private Long id;

    @NonNull
    private String name;

    @NonNull
    private String mac;
    private String userId;
    private String pwd;
    private Integer admin;
    private Integer uid;
    private Integer gid;
    private Integer domain;
    private Long time;
    private boolean isLogout;
    private boolean isActive;
    private boolean devInfoChanged;
    /**
     * username : admin
     * nickname : -
     * email : admin@onespace.cc
     * phone : -
     * role : 0
     * avatar :
     * remark : -
     * gender : 0
     * space : 10000
     * used : 0
     * encrypt :
     * isdelete : 0
     * login_time : 1527094117
     * create_at : 0
     */

    private String username;
    private String nickname;
    private String email;
    private String phone;
    private int role;
    private String avatar;
    @SerializedName(value = "mark",alternate = {"remark"})
    private String remark;
    private int gender;
    private long space;
    private long used;
    private String encrypt;
    private int isdelete;
    private long login_time;
    private long create_at;
    @SerializedName(value = "device_desc",alternate = {"devMarkName"})
    private String devMarkName;
    @SerializedName(value = "device_name",alternate = {"devMarkDesc"})
    private String devMarkDesc;
    @SerializedName("permissions")
    private List<PermissionsModel> permissions;


    public UserInfo(@NonNull String name, @NonNull String mac) {
        this.name = name;
        this.mac = mac;
    }

    public UserInfo(Long id) {
        this.id = id;
    }

    public UserInfo(Long id, @NonNull String name, @NonNull String mac, String pwd, Integer admin, Integer uid, Integer gid, Integer domain, Long time, boolean isLogout,
                    boolean isActive, boolean devInfoChanged, String username, String nickname, String email, String phone, int role, String avatar, String remark, int gender, long space,
                    long used, String encrypt, int isdelete, long login_time, long create_at, String devMarkName, String devMarkDesc) {
        this.id = id;
        this.name = name;
        this.mac = mac;
        this.pwd = pwd;
        this.admin = admin;
        this.uid = uid;
        this.gid = gid;
        this.domain = domain;
        this.time = time;
        this.isLogout = isLogout;
        this.isActive = isActive;
        this.devInfoChanged = devInfoChanged;
        this.username = username;
        this.nickname = nickname;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.avatar = avatar;
        this.remark = remark;
        this.gender = gender;
        this.space = space;
        this.used = used;
        this.encrypt = encrypt;
        this.isdelete = isdelete;
        this.login_time = login_time;
        this.create_at = create_at;
        this.devMarkName = devMarkName;
        this.devMarkDesc = devMarkDesc;
    }

    public UserInfo() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    /**
     * Not-null value; ensure this value is available before it is saved to the database.
     */
    public void setName(@NonNull String name) {
        this.name = name;
    }

    @NonNull
    public String getMac() {
        return mac;
    }

    /**
     * Not-null value; ensure this value is available before it is saved to the database.
     */
    public void setMac(@NonNull String mac) {
        this.mac = mac;
    }

    @NonNull
    public String getPwd() {
        return pwd;
    }

    /**
     * Not-null value; ensure this value is available before it is saved to the database.
     */
    public void setPwd(@NonNull String pwd) {
        this.pwd = pwd;
    }

    public Integer getAdmin() {
        return admin;
    }

    public void setAdmin(Integer admin) {
        this.admin = admin;
    }

    public Integer getUid() {
        return uid;
    }

    public void setUid(Integer uid) {
        this.uid = uid;
    }

    public Integer getGid() {
        return gid;
    }

    public void setGid(Integer gid) {
        this.gid = gid;
    }

    public Integer getDomain() {
        return domain;
    }

    public void setDomain(Integer domain) {
        this.domain = domain;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public boolean getIsLogout() {
        return isLogout;
    }

    public boolean getIsActive() {
        return isActive;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public long getSpace() {
        return space;
    }

    public void setSpace(long space) {
        this.space = space;
    }

    public long getUsed() {
        return used;
    }

    public void setUsed(long used) {
        this.used = used;
    }

    public String getEncrypt() {
        return encrypt;
    }

    public void setEncrypt(String encrypt) {
        this.encrypt = encrypt;
    }

    public int getIsdelete() {
        return isdelete;
    }

    public void setIsdelete(int isdelete) {
        this.isdelete = isdelete;
    }

    public long getLogin_time() {
        return login_time;
    }

    public void setLogin_time(long login_time) {
        this.login_time = login_time;
    }

    public long getCreate_at() {
        return create_at;
    }

    public void setCreate_at(long create_at) {
        this.create_at = create_at;
    }

    public String getDevMarkName() {
        return devMarkName;
    }

    public void setDevMarkName(String devMarkName) {
        this.devMarkName = devMarkName;
    }

    public String getDevMarkDesc() {
        return devMarkDesc;
    }

    public void setDevMarkDesc(String devMarkDesc) {
        this.devMarkDesc = devMarkDesc;
    }

    public boolean getDevInfoChanged() {
        return this.devInfoChanged;
    }

    public void setDevInfoChanged(boolean devInfoChanged) {
        this.devInfoChanged = devInfoChanged;
    }

    public void setIsLogout(boolean isLogout) {
        this.isLogout = isLogout;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }


    public List<PermissionsModel> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<PermissionsModel> permissions) {
        this.permissions = permissions;
    }

}
