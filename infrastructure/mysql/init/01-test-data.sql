-- ============================================================
-- KaiLei AI Secretary - 测试数据初始化脚本
-- 使用方法：
--   1. 启动 docker-compose 基础服务（MySQL）
--   2. 启动 Java 服务一次（AutoTable 自动建表）
--   3. 执行本 SQL 脚本插入测试数据
-- ============================================================

USE kai_lei_ai_secretary;

-- ============================================================
-- 1. 角色表 kl_role
-- ============================================================
INSERT INTO kl_role (id, name, role_key, description, status, create_by, create_time, update_by, update_time, is_delete) VALUES
(1, '管理员', 'admin',    '系统管理员，拥有所有权限', 1, 'system', NOW(), 'system', NOW(), 1),
(2, '运营',   'operation', '运营人员，可进行充值等操作', 1, 'system', NOW(), 'system', NOW(), 1),
(3, '普通用户', 'user',    '普通用户，可开始游戏等', 1, 'system', NOW(), 'system', NOW(), 1);

-- ============================================================
-- 2. 权限表 kl_permission（可选，目前权限校验未启用）
-- ============================================================
INSERT INTO kl_permission (id, name, permission_key, permission_type, resource_path, description, create_by, create_time, update_by, update_time, is_delete) VALUES
(1, '开始游戏',   'game:start',             'api', '/api/v1/game/startGame',          '允许开始游戏', 'system', NOW(), 'system', NOW(), 1),
(2, '充值',       'game:topUp',             'api', '/api/v1/game/topUp',              '允许充值操作', 'system', NOW(), 'system', NOW(), 1),
(3, '封号',       'game:accountSuspension', 'api', '/api/v1/game/accountSuspension',  '允许封号操作', 'system', NOW(), 'system', NOW(), 1);

-- ============================================================
-- 3. 用户表 kl_user
--    密码加密规则：MD5(明文密码 + 盐值)
--    zhangsan 密码: Test1234  盐值: k1A2b3C4d5  MD5: d03adee7451a3ea67d3b50710a7da206
--    lisi     密码: Admin5678 盐值: x9Y8z7W6v5  MD5: fe7c5cce7d931a7831f2dbfcbd149cc9
-- ============================================================
INSERT INTO kl_user (id, name, nickname, pass, salt_value, email, mobile, avatar_url, status, default_secretary_id, create_by, create_time, update_by, update_time, is_delete) VALUES
(1, 'zhangsan', '张三', 'd03adee7451a3ea67d3b50710a7da206', 'k1A2b3C4d5', 'zhangsan@example.com', '13800001111', NULL, 1, NULL, 'system', NOW(), 'system', NOW(), 1),
(2, 'lisi',     '李四', 'fe7c5cce7d931a7831f2dbfcbd149cc9', 'x9Y8z7W6v5', 'lisi@example.com',     '13800002222', NULL, 1, NULL, 'system', NOW(), 'system', NOW(), 1);

-- ============================================================
-- 4. 用户角色关联表 kl_user_role
--    zhangsan → user（普通用户）
--    lisi     → admin + operation（管理员+运营）
-- ============================================================
INSERT INTO kl_user_role (id, user_id, role_id, create_by, create_time, update_by, update_time, is_delete) VALUES
(1, 1, 3, 'system', NOW(), 'system', NOW(), 1),
(2, 2, 1, 'system', NOW(), 'system', NOW(), 1),
(3, 2, 2, 'system', NOW(), 'system', NOW(), 1);

-- ============================================================
-- 5. 角色权限关联表 kl_role_permission
-- ============================================================
INSERT INTO kl_role_permission (id, role_id, permission_id, create_by, create_time, update_by, update_time, is_delete) VALUES
(1, 3, 1, 'system', NOW(), 'system', NOW(), 1),  -- user 可以开始游戏
(2, 2, 2, 'system', NOW(), 'system', NOW(), 1),  -- operation 可以充值
(3, 1, 3, 'system', NOW(), 'system', NOW(), 1);  -- admin 可以封号

-- ============================================================
-- 6. 轮播图表 kl_banner
-- ============================================================
INSERT INTO kl_banner (id, img_url, link_url, remark, status, create_by, create_time, update_by, update_time, is_delete) VALUES
(1, 'https://placehold.co/800x400/4F46E5/FFFFFF?text=AI+Secretary', 'https://example.com/promo1', 'AI秘书宣传图', 1, 'system', NOW(), 'system', NOW(), 1),
(2, 'https://placehold.co/800x400/EC4899/FFFFFF?text=Smart+Workflow', 'https://example.com/promo2', '智能工作流',   1, 'system', NOW(), 'system', NOW(), 1),
(3, 'https://placehold.co/800x400/10B981/FFFFFF?text=Team+Collab',    'https://example.com/promo3', '团队协作',     1, 'system', NOW(), 'system', NOW(), 1);

-- ============================================================
-- 7. 产品表 kl_product
-- ============================================================
INSERT INTO kl_product (id, product_name, price, stock, type, status, create_by, create_time, update_by, update_time, is_delete) VALUES
(1, 'AI秘书基础版', 99.99, 1000, 'subscription', 1, 'system', NOW(), 'system', NOW(), 1),
(2, 'AI秘书专业版', 199.99, 500, 'subscription', 1, 'system', NOW(), 'system', NOW(), 1),
(3, '语音识别插件', 29.99, 2000, 'addon', 1, 'system', NOW(), 'system', NOW(), 1);

-- ============================================================
-- 8. AI秘书表 kl_ai_secretary
-- ============================================================
INSERT INTO kl_ai_secretary (id, user_id, secretary_name, avatar_url, role_prompt, model_provider, model_name, default_flag, status, create_by, create_time, update_by, update_time, is_delete) VALUES
(1, 1, '小凯', NULL, '你是一个专业的AI秘书，帮助用户处理日常工作和任务管理。', 'deepseek', 'deepseek-chat', 1, 1, 'system', NOW(), 'system', NOW(), 1);

-- ============================================================
-- 9. 会话表 kl_ai_conversation
-- ============================================================
INSERT INTO kl_ai_conversation (id, user_id, secretary_id, title, conversation_type, status, create_by, create_time, update_by, update_time, is_delete) VALUES
(1, 1, 1, '日常助手对话', 'user_secretary', 1, 'system', NOW(), 'system', NOW(), 1);

-- ============================================================
-- 10. 消息表 kl_ai_message
-- ============================================================
INSERT INTO kl_ai_message (id, conversation_id, sender_user_id, sender_secretary_id, receiver_user_id, receiver_secretary_id, message_role, message_type, content, status, create_by, create_time, update_by, update_time, is_delete) VALUES
(1, 1, 1, NULL, NULL, 1, 'user',      'text', '你好，帮我安排明天下午三点的会议', 1, 'system', NOW(), 'system', NOW(), 1),
(2, 1, NULL, 1,    1, NULL, 'assistant', 'text', '好的，已为您创建会议提醒。请问需要邀请哪些参会人员？', 1, 'system', NOW(), 'system', NOW(), 1);

-- ============================================================
-- 11. 待办事项表 kl_todo
-- ============================================================
INSERT INTO kl_todo (id, creator_user_id, receiver_user_id, title, content, todo_type, priority, deadline_time, status, create_by, create_time, update_by, update_time, is_delete) VALUES
(1, 1, 1, '完成季度报告', '需要在本周五之前提交Q2季度报告给部门经理', 'custom', 3, '2026-06-30 18:00:00', 0, 'system', NOW(), 'system', NOW(), 1),
(2, 2, 1, '审批采购申请', '张三提交的办公用品采购申请需要审批', 'review', 2, '2026-06-15 12:00:00', 0, 'system', NOW(), 'system', NOW(), 1);

-- ============================================================
-- 12. 订单表 kl_order & 物流表 kl_logistics
-- ============================================================
INSERT INTO kl_order (id, product_id, user_id, order_status, create_by, create_time, update_by, update_time, is_delete) VALUES
(1, 1, 1, 2, 'system', NOW(), 'system', NOW(), 1),
(2, 2, 2, 1, 'system', NOW(), 'system', NOW(), 1);

INSERT INTO kl_logistics (id, order_id, logistics_no, logistics_status, create_by, create_time, update_by, update_time, is_delete) VALUES
(1, 1, 'SF1234567890', 2, 'system', NOW(), 'system', NOW(), 1),
(2, 2, 'YT0987654321', 0, 'system', NOW(), 'system', NOW(), 1);

-- ============================================================
-- 验证插入结果
-- ============================================================
SELECT 'kl_role' AS table_name, COUNT(*) AS row_count FROM kl_role
UNION ALL
SELECT 'kl_permission', COUNT(*) FROM kl_permission
UNION ALL
SELECT 'kl_user', COUNT(*) FROM kl_user
UNION ALL
SELECT 'kl_user_role', COUNT(*) FROM kl_user_role
UNION ALL
SELECT 'kl_role_permission', COUNT(*) FROM kl_role_permission
UNION ALL
SELECT 'kl_banner', COUNT(*) FROM kl_banner
UNION ALL
SELECT 'kl_product', COUNT(*) FROM kl_product
UNION ALL
SELECT 'kl_ai_secretary', COUNT(*) FROM kl_ai_secretary
UNION ALL
SELECT 'kl_ai_conversation', COUNT(*) FROM kl_ai_conversation
UNION ALL
SELECT 'kl_ai_message', COUNT(*) FROM kl_ai_message
UNION ALL
SELECT 'kl_todo', COUNT(*) FROM kl_todo
UNION ALL
SELECT 'kl_order', COUNT(*) FROM kl_order
UNION ALL
SELECT 'kl_logistics', COUNT(*) FROM kl_logistics;
