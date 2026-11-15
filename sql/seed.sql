INSERT INTO ems.sys_role (id, role_code, role_name, sort_no, status, remark)
VALUES (1, 'ADMIN', '超级管理员', 1, 1, '系统内置角色'),
       (2, 'USER', '普通用户', 2, 1, '系统内置角色')
ON CONFLICT DO NOTHING;

INSERT INTO ems.sys_dept (id, parent_id, dept_code, dept_name, sort_no, status, remark)
VALUES (0, 0, 'COMPANY', '公司', 0, 1, '根公司'),
       (1, 0, 'DATA_CENTER', '数据中心', 1, 1, NULL)
ON CONFLICT (id) DO UPDATE SET parent_id   = EXCLUDED.parent_id,
                               dept_code   = EXCLUDED.dept_code,
                               dept_name   = EXCLUDED.dept_name,
                               sort_no     = EXCLUDED.sort_no,
                               status      = EXCLUDED.status,
                               remark      = EXCLUDED.remark,
                               update_time = now();

UPDATE ems.sys_user SET dept_id = 1 WHERE dept_id IN (2, 3) OR username = 'admin';
DELETE FROM ems.sys_dept WHERE id IN (2, 3);

INSERT INTO ems.sys_menu (id, parent_id, menu_code, menu_name, menu_type, path, component, permission, icon, sort_no, status)
VALUES (100, 0, 'dashboard', '仪表盘', 'MENU', '/dashboard', 'dashboard/index', 'dashboard', 'ri:home-2-line', 1, 1),
       (200, 0, 'system', '系统管理', 'DIR', '/system', NULL, NULL, 'ri:settings-3-line', 2, 1),
       (201, 200, 'system:user', '用户管理', 'MENU', '/system/user', 'system/user/index', 'system:user:list', 'ri:user-3-line', 1, 1),
       (202, 200, 'system:role', '角色管理', 'MENU', '/system/role', 'system/role/index', 'system:role:list', 'ri:shield-user-line', 2, 1),
       (203, 200, 'system:menu', '菜单管理', 'MENU', '/system/menu', 'system/menu/index', 'system:menu:list', 'ri:menu-line', 3, 1),
       (204, 200, 'system:dept', '部门管理', 'MENU', '/system/dept', 'system/dept/index', 'system:dept:list', 'ri:building-2-line', 4, 1),
       (205, 200, 'system:dict', '字典管理', 'MENU', '/system/dict', 'system/dict/index', 'system:dict:list', 'ri:book-2-line', 5, 1),
       (300, 0, 'eda', 'EDA管理', 'DIR', '/eda', NULL, NULL, 'ri:cpu-line', 3, 1),
       (301, 300, 'eda:list', 'EDA列表', 'MENU', '/eda/list', 'eda/list/index', 'eda:list', 'ri:list-check', 1, 1)
ON CONFLICT DO NOTHING;

INSERT INTO ems.sys_dict_type (id, dict_type, dict_name, status, remark)
VALUES (1, 'sys_normal_disable', '系统状态', 1, NULL),
       (2, 'sys_user_gender', '用户性别', 1, NULL),
       (3, 'eda_os_type', '操作系统', 1, NULL),
       (4, 'eda_license_status', 'License 状态', 1, NULL)
ON CONFLICT DO NOTHING;

INSERT INTO ems.sys_dict_data (id, dict_type, label, dict_value, sort_no, status)
VALUES (1, 'sys_normal_disable', '停用', '0', 1, 1),
       (2, 'sys_normal_disable', '启用', '1', 2, 1),
       (3, 'sys_user_gender', '未知', '0', 1, 1),
       (4, 'sys_user_gender', '男', '1', 2, 1),
       (5, 'sys_user_gender', '女', '2', 3, 1),
       (6, 'eda_os_type', 'Linux', 'Linux', 1, 1),
       (7, 'eda_os_type', 'Windows', 'Windows', 2, 1),
       (8, 'eda_license_status', '有效', 'VALID', 1, 1),
       (9, 'eda_license_status', '已过期', 'EXPIRED', 2, 1),
       (10, 'eda_license_status', '未知', 'UNKNOWN', 3, 1)
ON CONFLICT DO NOTHING;

INSERT INTO ems.sys_user (id, username, password, real_name, gender, email, dept_id, role_code, status, remark)
VALUES (1, 'admin', '$2b$10$An4HDDX2GAgLVGQ5cCOpluv0kd0VFYsmFugLY3Gnpz.WdSTzZWNHm',
        '管理员', 1, 'admin@ems.local', 1, 'ADMIN', 1, '系统内置管理员')
ON CONFLICT DO NOTHING;

INSERT INTO ems.eda_tool (id, tool_name, tool_type, host, os_type, tool_version, license_status, status, last_heartbeat, remark)
VALUES (1, 'Cadence Virtuoso', '模拟设计', 'eda-linux-01', 'Linux', 'IC618', 'VALID', 1, now() - interval '3 minutes', NULL),
       (2, 'Synopsys Design Compiler', '逻辑综合', 'eda-linux-02', 'Linux', 'V-2023.12', 'VALID', 1, now() - interval '8 minutes', NULL),
       (3, '嘉立创EDA专业版', 'PCB 设计', 'eda-win-01', 'Windows', '2.2.42.9', 'UNKNOWN', 0, now() - interval '2 hours', NULL)
ON CONFLICT DO NOTHING;

SELECT setval(pg_get_serial_sequence('ems.sys_user', 'id'), COALESCE(MAX(id), 0) + 1, false) FROM ems.sys_user;
SELECT setval(pg_get_serial_sequence('ems.sys_role', 'id'), COALESCE(MAX(id), 0) + 1, false) FROM ems.sys_role;
SELECT setval(pg_get_serial_sequence('ems.sys_menu', 'id'), COALESCE(MAX(id), 0) + 1, false) FROM ems.sys_menu;
SELECT setval(pg_get_serial_sequence('ems.sys_dept', 'id'), COALESCE(MAX(id), 0) + 1, false) FROM ems.sys_dept;
SELECT setval(pg_get_serial_sequence('ems.sys_dict_type', 'id'), COALESCE(MAX(id), 0) + 1, false) FROM ems.sys_dict_type;
SELECT setval(pg_get_serial_sequence('ems.sys_dict_data', 'id'), COALESCE(MAX(id), 0) + 1, false) FROM ems.sys_dict_data;
SELECT setval(pg_get_serial_sequence('ems.eda_tool', 'id'), COALESCE(MAX(id), 0) + 1, false) FROM ems.eda_tool;
