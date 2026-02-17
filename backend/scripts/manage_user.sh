#!/bin/bash
# 用户管理脚本
# 用法: ./manage_user.sh create <用户名> <密码> <角色>
#       ./manage_user.sh delete <用户名>
#       ./manage_user.sh list

DB_USER="xie080886"
DB_PASS="xie080886"
DB_NAME="xie080886"

# 生成BCrypt密码哈希
generate_bcrypt() {
    local password=$1
    cd /root/sscp28/backend
    # Compile + run GenBcrypt with arg; avoids stale classes and source edits.
    hash=$(mvn -q -DskipTests compile exec:java -Dexec.mainClass="GenBcrypt" -Dexec.args="$password" 2>/dev/null | grep -E '^\$2[aby]\$[0-9]{2}\$')
    echo "$hash"
}

# 创建用户
create_user() {
    local username=$1
    local password=$2
    local role=${3:-MEMBER}  # 默认MEMBER
    
    if [ -z "$username" ] || [ -z "$password" ]; then
        echo "错误: 用户名和密码不能为空"
        echo "用法: $0 create <用户名> <密码> [角色]"
        exit 1
    fi
    
    echo "正在为密码生成BCrypt哈希..."
    password_hash=$(generate_bcrypt "$password")
    
    if [ -z "$password_hash" ]; then
        echo "错误: 密码哈希生成失败"
        exit 1
    fi
    
    echo "密码哈希: ${password_hash:0:30}..."
    echo "正在创建用户 $username (角色: $role)..."
    
mysql -u$DB_USER -p$DB_PASS $DB_NAME << EOF
INSERT INTO users (username, password, role, enabled, password_changed, login_count_without_change, created_at, updated_at) 
VALUES (
    '$username', 
    '$password_hash',
    '$role', 
    1, 
    0,
    0,
    NOW(), 
    NOW()
)
ON DUPLICATE KEY UPDATE 
    password = '$password_hash',
    role = '$role',
    password_changed = 0,
    login_count_without_change = 0,
    updated_at = NOW();

SELECT id, username, role, CAST(enabled AS UNSIGNED) as enabled, CAST(password_changed AS UNSIGNED) as password_changed, created_at 
FROM users 
WHERE username = '$username';
EOF
    
    echo "✅ 用户创建成功!"
}

# 删除用户
delete_user() {
    local username=$1
    
    if [ -z "$username" ]; then
        echo "错误: 用户名不能为空"
        echo "用法: $0 delete <用户名>"
        exit 1
    fi
    
    echo "正在删除用户 $username..."
    
    mysql -u$DB_USER -p$DB_PASS $DB_NAME << EOF
DELETE FROM users WHERE username = '$username';
SELECT ROW_COUNT() as deleted_count;
EOF
    
    echo "✅ 用户删除完成!"
}

# 列出所有用户
list_users() {
    echo "数据库中的所有用户:"
    mysql -u$DB_USER -p$DB_PASS $DB_NAME << EOF
SELECT 
    id, 
    username, 
    role, 
    CAST(enabled AS UNSIGNED) as enabled, 
    CAST(password_changed AS UNSIGNED) as password_changed,
    created_at,
    updated_at
FROM users 
ORDER BY id;
EOF
}

# 清空所有用户
clear_all() {
    echo "⚠️  警告: 这将删除所有用户!"
    read -p "确认要清空所有用户吗? (yes/no): " confirm
    
    if [ "$confirm" = "yes" ]; then
        mysql -u$DB_USER -p$DB_PASS $DB_NAME << EOF
DELETE FROM users;
SELECT '✅ 所有用户已清空' as result;
EOF
    else
        echo "操作已取消"
    fi
}

# 主程序
case "$1" in
    create)
        create_user "$2" "$3" "$4"
        ;;
    delete)
        delete_user "$2"
        ;;
    list)
        list_users
        ;;
    clear)
        clear_all
        ;;
    *)
        echo "BCBBS 用户管理工具"
        echo ""
        echo "用法:"
        echo "  $0 create <用户名> <密码> [角色]  - 创建或更新用户"
        echo "  $0 delete <用户名>                - 删除用户"
        echo "  $0 list                           - 列出所有用户"
        echo "  $0 clear                          - 清空所有用户"
        echo ""
        echo "角色选项: MEMBER (默认), AGENT, ADMIN"
        echo ""
        echo "示例:"
        echo "  $0 create AA1010 AA1011 MEMBER"
        echo "  $0 create admin123 pass123 ADMIN"
        echo "  $0 delete AA1010"
        echo "  $0 list"
        exit 1
        ;;
esac
