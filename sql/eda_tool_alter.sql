-- eda_tool 关联服务器（幂等；init.sql 已同步）
ALTER TABLE ems.eda_tool ADD COLUMN IF NOT EXISTS server_id BIGINT;

CREATE INDEX IF NOT EXISTS idx_eda_tool_server ON ems.eda_tool (server_id);

COMMENT ON COLUMN ems.eda_tool.server_id IS '关联服务器 eda_server.id；为空时不能执行脚本';
