package com.seniorcareplus.app.models

/**
 * 用戶資料模型，包含所有用戶個人資訊
 */
data class UserProfile(
    val username: String,
    val chineseName: String? = null,  // 中文姓名
    val englishName: String? = null,  // 英文姓名
    val email: String? = null,
    val birthday: String? = null,
    val gender: Int = 0,  // 0=未設定，1=男，2=女，3=其他
    val phoneNumber: String? = null,
    val address: String? = null,
    val accountType: Int = 1,  // 1=院友，2=家屬，3=員工，4=管理人員，5=開發人員
    val profilePhotoUri: String? = null
) {
    /**
     * 獲取帳號類型的名稱
     * @param isChinese 是否返回中文名稱
     */
    fun getAccountTypeName(isChinese: Boolean = true): String {
        return when (accountType) {
            1 -> if (isChinese) "院友" else "Patient"
            2 -> if (isChinese) "家屬" else "Family"
            3 -> if (isChinese) "員工" else "Staff"
            4 -> if (isChinese) "管理人員" else "Admin"
            5 -> if (isChinese) "開發人員" else "Developer"
            else -> if (isChinese) "未設定" else "Unspecified"
        }
    }
    
    /**
     * 獲取性別名稱
     * @param isChinese 是否返回中文名稱
     */
    fun getGenderName(isChinese: Boolean = true): String {
        return when (gender) {
            1 -> if (isChinese) "男" else "Male"
            2 -> if (isChinese) "女" else "Female"
            3 -> if (isChinese) "其他" else "Other"
            else -> if (isChinese) "未設定" else "Unspecified"
        }
    }
    
    /**
     * 檢查是否為特殊權限帳號（管理人員或開發人員）
     */
    fun isPrivilegedAccount(): Boolean {
        return accountType >= 4
    }
    
    /**
     * 檢查是否為工作人員（員工，管理人員或開發人員）
     */
    fun isStaffMember(): Boolean {
        return accountType >= 3
    }
    
    companion object {
        // 帳號類型常數
        const val ACCOUNT_TYPE_PATIENT = 1
        const val ACCOUNT_TYPE_FAMILY = 2
        const val ACCOUNT_TYPE_STAFF = 3
        const val ACCOUNT_TYPE_ADMIN = 4
        const val ACCOUNT_TYPE_DEVELOPER = 5
        
        // 性別常數
        const val GENDER_UNSPECIFIED = 0
        const val GENDER_MALE = 1
        const val GENDER_FEMALE = 2
        const val GENDER_OTHER = 3
    }
}
