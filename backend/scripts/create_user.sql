-- 创建用户的SQL脚本
-- 使用方法: mysql -uxie080886 -pxie080886 xie080886 < create_user.sql

-- 删除所有用户（可选，如果需要清空）
-- DELETE FROM users;

-- 创建会员用户 AA1010
-- 密码: AA1011 (BCrypt哈希)
INSERT INTO users (username, password, role, enabled, password_changed, created_at, updated_at) 
VALUES (
    'AA1010', 
    '$2a$10$zY2Cl5jyAfSjteXd7l/m2.XQfQKQq5spmDAQFBtNQnTVSOMciAF7G',  -- AA1011
    'MEMBER', 
    1, 
    1, 
    NOW(), 
    NOW()
)
ON DUPLICATE KEY UPDATE 
    password = '$2a$10$zY2Cl5jyAfSjteXd7l/m2.XQfQKQq5spmDAQFBtNQnTVSOMciAF7G',
    updated_at = NOW();

-- 查看创建的用户
SELECT id, username, role, enabled, password_changed, created_at 
FROM users 
WHERE username = 'AA1010';
