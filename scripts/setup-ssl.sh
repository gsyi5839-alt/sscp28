#!/bin/bash

# SSL证书申请和自动续期配置脚本
# 用于域名: 18118bw.cn

set -e

DOMAIN="18118bw.cn"
WWW_DOMAIN="www.18118bw.cn"
EMAIL="admin@18118bw.cn"  # 请修改为您的邮箱

echo "======================================="
echo "SSL证书申请脚本"
echo "域名: $DOMAIN, $WWW_DOMAIN"
echo "======================================="
echo ""

# 检查Certbot是否已安装
if ! command -v certbot &> /dev/null; then
    echo "❌ Certbot未安装，请先运行: apt install -y certbot python3-certbot-nginx"
    exit 1
fi

echo "✅ Certbot已安装"

# 检查Nginx是否运行
if ! systemctl is-active --quiet nginx; then
    echo "❌ Nginx未运行，正在启动..."
    systemctl start nginx
fi

echo "✅ Nginx正在运行"

# 检查80端口是否开放
if ! netstat -tuln | grep -q ":80 "; then
    echo "❌ 端口80未监听，请检查Nginx配置"
    exit 1
fi

echo "✅ 端口80已开放"

# 申请证书
echo ""
echo "🔐 开始申请SSL证书..."
echo "⚠️  请确保域名已解析到当前服务器IP"
echo ""
read -p "域名是否已经解析？(y/n) " -n 1 -r
echo ""

if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "❌ 请先配置域名解析后再运行此脚本"
    echo ""
    echo "域名解析配置："
    echo "  记录类型: A"
    echo "  主机记录: @ 和 www"
    echo "  记录值: $(curl -s ifconfig.me)"
    exit 1
fi

# 使用Certbot申请证书
certbot --nginx \
    -d "$DOMAIN" \
    -d "$WWW_DOMAIN" \
    --email "$EMAIL" \
    --agree-tos \
    --no-eff-email \
    --redirect

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ SSL证书申请成功！"
    echo ""
    
    # 配置自动续期
    echo "🔄 配置自动续期..."
    
    # 创建续期测试
    certbot renew --dry-run
    
    if [ $? -eq 0 ]; then
        echo "✅ 自动续期测试成功！"
        echo ""
        echo "📋 自动续期信息："
        echo "  - Certbot会自动创建systemd timer进行续期"
        echo "  - 查看续期任务: systemctl list-timers"
        echo "  - 手动测试续期: certbot renew --dry-run"
        echo "  - 强制续期: certbot renew --force-renewal"
        echo ""
        
        # 检查systemd timer状态
        if systemctl list-timers | grep -q certbot; then
            echo "✅ Certbot自动续期已启用"
            systemctl list-timers | grep certbot
        else
            echo "⚠️  Certbot timer未找到，手动创建..."
            
            # 创建systemd timer（备用）
            cat > /etc/systemd/system/certbot-renewal.timer << 'EOF'
[Unit]
Description=Certbot SSL证书自动续期定时器

[Timer]
OnCalendar=daily
RandomizedDelaySec=1h
Persistent=true

[Install]
WantedBy=timers.target
EOF

            cat > /etc/systemd/system/certbot-renewal.service << 'EOF'
[Unit]
Description=Certbot SSL证书续期服务

[Service]
Type=oneshot
ExecStart=/usr/bin/certbot renew --quiet --post-hook "systemctl reload nginx"
EOF

            systemctl daemon-reload
            systemctl enable certbot-renewal.timer
            systemctl start certbot-renewal.timer
            
            echo "✅ 已创建并启动Certbot自动续期定时器"
        fi
        
        echo ""
        echo "🎉 SSL证书配置完成！"
        echo ""
        echo "证书信息："
        certbot certificates
        
    else
        echo "❌ 自动续期测试失败"
        exit 1
    fi
else
    echo "❌ SSL证书申请失败"
    echo ""
    echo "常见问题："
    echo "1. 域名未正确解析到当前服务器"
    echo "2. 防火墙阻止了80或443端口"
    echo "3. Nginx配置错误"
    echo ""
    echo "请检查后重试"
    exit 1
fi

echo ""
echo "======================================="
echo "✅ 所有配置已完成！"
echo "======================================="
echo ""
echo "您现在可以通过以下地址访问："
echo "  - https://$DOMAIN"
echo "  - https://$WWW_DOMAIN"
echo ""
