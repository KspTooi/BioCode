-- 用户组、权限码、在线用户 路由域由 core 调整为 auth
UPDATE core_menu
SET path = '/auth/group-manager',
    update_time = NOW()
WHERE delete_time IS NULL
  AND path = '/core/group-manager';

UPDATE core_menu
SET path = '/auth/permission-manager',
    update_time = NOW()
WHERE delete_time IS NULL
  AND path = '/core/permission-manager';

UPDATE core_menu
SET path = '/auth/session-manager',
    update_time = NOW()
WHERE delete_time IS NULL
  AND path = '/core/session-manager';
