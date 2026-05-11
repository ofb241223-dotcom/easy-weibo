INSERT INTO users (id, username, password, nickname, avatar_url, cover_url, bio, role, status, mute_status, created_at) VALUES
  (1, 'admin', '$2a$10$.K7ZcovHlVYAKBkH3UqdVOWT1PcJmeHFA9iJcaP9mIcG/uK10Bclm', 'HNUST Admin', 'https://picsum.photos/seed/admin/200', 'https://picsum.photos/seed/admin-cover/1200/400', 'Official admin of HNUST Easy WeiBo.', 'ADMIN', 'ACTIVE', 'NORMAL', DATE_SUB(NOW(), INTERVAL 240 DAY)),
  (2, 'johndoe', '$2a$10$.K7ZcovHlVYAKBkH3UqdVOWT1PcJmeHFA9iJcaP9mIcG/uK10Bclm', 'John Doe', 'https://picsum.photos/seed/john/200', 'https://picsum.photos/seed/john-cover/1200/400', '图书馆常驻选手。', 'USER', 'ACTIVE', 'NORMAL', DATE_SUB(NOW(), INTERVAL 150 DAY)),
  (3, 'janedoe', '$2a$10$.K7ZcovHlVYAKBkH3UqdVOWT1PcJmeHFA9iJcaP9mIcG/uK10Bclm', 'Jane Doe', 'https://picsum.photos/seed/jane/200', 'https://picsum.photos/seed/jane-cover/1200/400', '热爱代码与校园生活。', 'USER', 'ACTIVE', 'NORMAL', DATE_SUB(NOW(), INTERVAL 145 DAY)),
  (4, 'alice', '$2a$10$.K7ZcovHlVYAKBkH3UqdVOWT1PcJmeHFA9iJcaP9mIcG/uK10Bclm', 'Alice Chen', 'https://picsum.photos/seed/alice/200', 'https://picsum.photos/seed/alice-cover/1200/400', '计算机学院 24 级，喜欢做产品设计。', 'USER', 'ACTIVE', 'NORMAL', DATE_SUB(NOW(), INTERVAL 120 DAY)),
  (5, 'bob', '$2a$10$.K7ZcovHlVYAKBkH3UqdVOWT1PcJmeHFA9iJcaP9mIcG/uK10Bclm', 'Bob Li', 'https://picsum.photos/seed/bob/200', 'https://picsum.photos/seed/bob-cover/1200/400', '喜欢篮球和数据库。', 'USER', 'ACTIVE', 'NORMAL', DATE_SUB(NOW(), INTERVAL 118 DAY)),
  (6, 'carol', '$2a$10$.K7ZcovHlVYAKBkH3UqdVOWT1PcJmeHFA9iJcaP9mIcG/uK10Bclm', 'Carol Wang', 'https://picsum.photos/seed/carol/200', 'https://picsum.photos/seed/carol-cover/1200/400', '正在准备数学建模比赛。', 'USER', 'ACTIVE', 'NORMAL', DATE_SUB(NOW(), INTERVAL 110 DAY)),
  (7, 'david', '$2a$10$.K7ZcovHlVYAKBkH3UqdVOWT1PcJmeHFA9iJcaP9mIcG/uK10Bclm', 'David Hu', 'https://picsum.photos/seed/david/200', 'https://picsum.photos/seed/david-cover/1200/400', '社团活动和前端开发都想兼顾。', 'USER', 'ACTIVE', 'NORMAL', DATE_SUB(NOW(), INTERVAL 100 DAY)),
  (8, 'emma', '$2a$10$.K7ZcovHlVYAKBkH3UqdVOWT1PcJmeHFA9iJcaP9mIcG/uK10Bclm', 'Emma Zhou', 'https://picsum.photos/seed/emma/200', 'https://picsum.photos/seed/emma-cover/1200/400', '记录校园里的每一次活动。', 'USER', 'ACTIVE', 'NORMAL', DATE_SUB(NOW(), INTERVAL 96 DAY)),
  (9, 'frank', '$2a$10$.K7ZcovHlVYAKBkH3UqdVOWT1PcJmeHFA9iJcaP9mIcG/uK10Bclm', 'Frank Luo', 'https://picsum.photos/seed/frank/200', 'https://picsum.photos/seed/frank-cover/1200/400', '热衷开源和 Linux。', 'USER', 'ACTIVE', 'NORMAL', DATE_SUB(NOW(), INTERVAL 88 DAY)),
  (10, 'grace', '$2a$10$.K7ZcovHlVYAKBkH3UqdVOWT1PcJmeHFA9iJcaP9mIcG/uK10Bclm', 'Grace Yu', 'https://picsum.photos/seed/grace/200', 'https://picsum.photos/seed/grace-cover/1200/400', '喜欢拍照片，也喜欢跑步。', 'USER', 'ACTIVE', 'NORMAL', DATE_SUB(NOW(), INTERVAL 80 DAY)),
  (11, 'henry', '$2a$10$.K7ZcovHlVYAKBkH3UqdVOWT1PcJmeHFA9iJcaP9mIcG/uK10Bclm', 'Henry Sun', 'https://picsum.photos/seed/henry/200', 'https://picsum.photos/seed/henry-cover/1200/400', '每天都在找安静自习室。', 'USER', 'ACTIVE', 'NORMAL', DATE_SUB(NOW(), INTERVAL 70 DAY)),
  (12, 'iris', '$2a$10$.K7ZcovHlVYAKBkH3UqdVOWT1PcJmeHFA9iJcaP9mIcG/uK10Bclm', 'Iris Lin', 'https://picsum.photos/seed/iris/200', 'https://picsum.photos/seed/iris-cover/1200/400', '研究生备考中，也写点后端。', 'USER', 'ACTIVE', 'NORMAL', DATE_SUB(NOW(), INTERVAL 66 DAY));

INSERT INTO posts (id, author_id, content, status, likes_count, reposts_count, comments_count, views_count, created_at) VALUES
  (1, 1, 'Welcome to HNUST Easy WeiBo! Share your thoughts and connect with others. #HNUST #Welcome', 'ACTIVE', 0, 0, 0, 0, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
  (2, 2, 'The library is so crowded today! Does anyone know a quiet place to study? 📚', 'ACTIVE', 0, 0, 0, 0, DATE_SUB(NOW(), INTERVAL 5 HOUR)),
  (3, 3, 'Just finished my first React project! It was challenging but fun. 💻✨ #Coding #React', 'ACTIVE', 0, 0, 0, 0, DATE_SUB(NOW(), INTERVAL 1 DAY)),
  (4, 4, '今晚的校园晚霞真的太好看了，操场边一整排都在拍照。 #校园日常', 'ACTIVE', 0, 0, 0, 0, DATE_SUB(NOW(), INTERVAL 28 HOUR)),
  (5, 5, '数据库实验终于做完了，谁懂把索引调通的快乐。 #MySQL', 'ACTIVE', 0, 0, 0, 0, DATE_SUB(NOW(), INTERVAL 32 HOUR)),
  (6, 6, '数学建模队今晚还在机房，外卖已经点第三次了。', 'ACTIVE', 0, 0, 0, 0, DATE_SUB(NOW(), INTERVAL 36 HOUR)),
  (7, 7, '社团招新摊位布置好了，明天操场见！ #招新', 'ACTIVE', 0, 0, 0, 0, DATE_SUB(NOW(), INTERVAL 42 HOUR)),
  (8, 8, '南门那家米粉今天居然排队排到马路边。', 'ACTIVE', 0, 0, 0, 0, DATE_SUB(NOW(), INTERVAL 2 DAY)),
  (9, 9, 'Linux 课设通过的那一瞬间，感觉这周都值了。 #Linux', 'ACTIVE', 0, 0, 0, 0, DATE_SUB(NOW(), INTERVAL 3 DAY)),
  (10, 10, '今天跑步打卡 5 公里，顺手拍了张暮色下的图书馆。 #跑步', 'ACTIVE', 0, 0, 0, 0, DATE_SUB(NOW(), INTERVAL 3 DAY)),
  (11, 11, '求一个安静自习点，寝室楼下今天又在排练。', 'ACTIVE', 0, 0, 0, 0, DATE_SUB(NOW(), INTERVAL 4 DAY)),
  (12, 12, '后端接口联调成功，今天可以安心睡觉了。 #SpringBoot', 'ACTIVE', 0, 0, 0, 0, DATE_SUB(NOW(), INTERVAL 4 DAY)),
  (13, 2, '下午在创新创业楼蹭到了一个空会议室，终于把小组汇报讲完。', 'ACTIVE', 0, 0, 0, 0, DATE_SUB(NOW(), INTERVAL 5 DAY)),
  (14, 3, '给社团做的新海报发出来了，欢迎拍砖。 #设计', 'ACTIVE', 0, 0, 0, 0, DATE_SUB(NOW(), INTERVAL 5 DAY)),
  (15, 4, '食堂新品的椒麻鸡还不错，但是排队真的是噩梦。', 'ACTIVE', 0, 0, 0, 0, DATE_SUB(NOW(), INTERVAL 6 DAY)),
  (16, 8, '今晚群里又在转发领奖名单，建议大家先确认下自己能不能去。', 'ACTIVE', 0, 0, 0, 0, DATE_SUB(NOW(), INTERVAL 6 DAY)),
  (17, 7, '明天上午十点操场东门集合，别迟到。', 'WITHDRAWN', 0, 0, 0, 0, DATE_SUB(NOW(), INTERVAL 7 DAY)),
  (18, 9, '有人把旧版实验答案发在群里了，先别乱传。', 'DELETED', 0, 0, 0, 0, DATE_SUB(NOW(), INTERVAL 8 DAY));

INSERT INTO post_images (post_id, image_url) VALUES
  (1, 'https://picsum.photos/seed/campus/800/400'),
  (3, 'https://picsum.photos/seed/code/800/400'),
  (3, 'https://picsum.photos/seed/react/800/400'),
  (4, 'https://picsum.photos/seed/sunset/800/400'),
  (10, 'https://picsum.photos/seed/library-run/800/400'),
  (14, 'https://picsum.photos/seed/poster/800/400');

INSERT INTO comments (post_id, author_id, content, likes_count, created_at) VALUES
  (1, 2, 'Great to be here!', 2, DATE_SUB(NOW(), INTERVAL 100 MINUTE)),
  (1, 3, '终于有我们自己的校园微博了。', 1, DATE_SUB(NOW(), INTERVAL 92 MINUTE)),
  (2, 11, '我今天在工科楼 4 楼找到一个空教室。', 0, DATE_SUB(NOW(), INTERVAL 220 MINUTE)),
  (2, 1, '可以去图书馆 B 区靠窗那边试试。', 3, DATE_SUB(NOW(), INTERVAL 210 MINUTE)),
  (3, 12, 'React 首个项目完成真的很有成就感！', 1, DATE_SUB(NOW(), INTERVAL 20 HOUR)),
  (4, 8, '晚霞真的很绝，操场那边一排都是人。', 0, DATE_SUB(NOW(), INTERVAL 27 HOUR)),
  (5, 9, '索引一开，执行计划直接顺眼了。', 1, DATE_SUB(NOW(), INTERVAL 30 HOUR)),
  (6, 5, '我们队昨晚也是到一点。', 0, DATE_SUB(NOW(), INTERVAL 33 HOUR)),
  (7, 4, '摊位需要海报的话可以找我。', 0, DATE_SUB(NOW(), INTERVAL 39 HOUR)),
  (8, 10, '南门米粉永远是热门。', 0, DATE_SUB(NOW(), INTERVAL 45 HOUR)),
  (9, 12, 'Linux 课设过了值得庆祝一下。', 0, DATE_SUB(NOW(), INTERVAL 70 HOUR)),
  (10, 3, '暮色下的图书馆很好看，求原图。', 0, DATE_SUB(NOW(), INTERVAL 73 HOUR)),
  (11, 2, '我一般去实验楼顶层。', 0, DATE_SUB(NOW(), INTERVAL 95 HOUR)),
  (12, 1, '接口通了记得补文档。', 2, DATE_SUB(NOW(), INTERVAL 98 HOUR)),
  (13, 6, '会议室是真的难抢。', 0, DATE_SUB(NOW(), INTERVAL 118 HOUR)),
  (14, 4, '配色很舒服，字也挺清楚。', 1, DATE_SUB(NOW(), INTERVAL 119 HOUR));

INSERT INTO comment_images (comment_id, image_url) VALUES
  (6, 'https://picsum.photos/seed/comment-sunset/480/320'),
  (10, 'https://picsum.photos/seed/comment-library/480/320');

INSERT INTO follows (follower_id, following_id, created_at) VALUES
  (1, 2, DATE_SUB(NOW(), INTERVAL 20 DAY)),
  (1, 3, DATE_SUB(NOW(), INTERVAL 19 DAY)),
  (1, 4, DATE_SUB(NOW(), INTERVAL 18 DAY)),
  (1, 5, DATE_SUB(NOW(), INTERVAL 17 DAY)),
  (2, 1, DATE_SUB(NOW(), INTERVAL 18 DAY)),
  (2, 3, DATE_SUB(NOW(), INTERVAL 17 DAY)),
  (2, 4, DATE_SUB(NOW(), INTERVAL 16 DAY)),
  (3, 1, DATE_SUB(NOW(), INTERVAL 18 DAY)),
  (3, 2, DATE_SUB(NOW(), INTERVAL 17 DAY)),
  (3, 6, DATE_SUB(NOW(), INTERVAL 14 DAY)),
  (4, 1, DATE_SUB(NOW(), INTERVAL 16 DAY)),
  (4, 2, DATE_SUB(NOW(), INTERVAL 14 DAY)),
  (4, 5, DATE_SUB(NOW(), INTERVAL 12 DAY)),
  (4, 7, DATE_SUB(NOW(), INTERVAL 11 DAY)),
  (5, 1, DATE_SUB(NOW(), INTERVAL 13 DAY)),
  (5, 4, DATE_SUB(NOW(), INTERVAL 12 DAY)),
  (5, 6, DATE_SUB(NOW(), INTERVAL 10 DAY)),
  (5, 8, DATE_SUB(NOW(), INTERVAL 9 DAY)),
  (6, 3, DATE_SUB(NOW(), INTERVAL 11 DAY)),
  (6, 5, DATE_SUB(NOW(), INTERVAL 9 DAY)),
  (6, 7, DATE_SUB(NOW(), INTERVAL 8 DAY)),
  (7, 4, DATE_SUB(NOW(), INTERVAL 8 DAY)),
  (7, 6, DATE_SUB(NOW(), INTERVAL 7 DAY)),
  (7, 9, DATE_SUB(NOW(), INTERVAL 6 DAY)),
  (8, 5, DATE_SUB(NOW(), INTERVAL 7 DAY)),
  (8, 9, DATE_SUB(NOW(), INTERVAL 6 DAY)),
  (8, 10, DATE_SUB(NOW(), INTERVAL 5 DAY)),
  (9, 7, DATE_SUB(NOW(), INTERVAL 6 DAY)),
  (9, 8, DATE_SUB(NOW(), INTERVAL 5 DAY)),
  (9, 10, DATE_SUB(NOW(), INTERVAL 4 DAY)),
  (9, 11, DATE_SUB(NOW(), INTERVAL 4 DAY)),
  (10, 8, DATE_SUB(NOW(), INTERVAL 4 DAY)),
  (10, 9, DATE_SUB(NOW(), INTERVAL 4 DAY)),
  (10, 12, DATE_SUB(NOW(), INTERVAL 3 DAY)),
  (11, 9, DATE_SUB(NOW(), INTERVAL 3 DAY)),
  (11, 12, DATE_SUB(NOW(), INTERVAL 3 DAY)),
  (12, 10, DATE_SUB(NOW(), INTERVAL 2 DAY)),
  (12, 11, DATE_SUB(NOW(), INTERVAL 2 DAY));

INSERT INTO post_likes (post_id, user_id, created_at) VALUES
  (1, 2, DATE_SUB(NOW(), INTERVAL 110 MINUTE)),
  (1, 3, DATE_SUB(NOW(), INTERVAL 108 MINUTE)),
  (1, 4, DATE_SUB(NOW(), INTERVAL 106 MINUTE)),
  (1, 5, DATE_SUB(NOW(), INTERVAL 104 MINUTE)),
  (2, 1, DATE_SUB(NOW(), INTERVAL 230 MINUTE)),
  (2, 11, DATE_SUB(NOW(), INTERVAL 220 MINUTE)),
  (2, 12, DATE_SUB(NOW(), INTERVAL 210 MINUTE)),
  (3, 1, DATE_SUB(NOW(), INTERVAL 22 HOUR)),
  (3, 4, DATE_SUB(NOW(), INTERVAL 21 HOUR)),
  (3, 8, DATE_SUB(NOW(), INTERVAL 20 HOUR)),
  (4, 1, DATE_SUB(NOW(), INTERVAL 28 HOUR)),
  (4, 8, DATE_SUB(NOW(), INTERVAL 27 HOUR)),
  (5, 9, DATE_SUB(NOW(), INTERVAL 31 HOUR)),
  (5, 12, DATE_SUB(NOW(), INTERVAL 30 HOUR)),
  (6, 5, DATE_SUB(NOW(), INTERVAL 34 HOUR)),
  (7, 1, DATE_SUB(NOW(), INTERVAL 42 HOUR)),
  (8, 10, DATE_SUB(NOW(), INTERVAL 47 HOUR)),
  (9, 12, DATE_SUB(NOW(), INTERVAL 72 HOUR)),
  (10, 3, DATE_SUB(NOW(), INTERVAL 75 HOUR)),
  (12, 1, DATE_SUB(NOW(), INTERVAL 99 HOUR)),
  (14, 4, DATE_SUB(NOW(), INTERVAL 121 HOUR));

INSERT INTO bookmarks (post_id, user_id, created_at) VALUES
  (1, 1, DATE_SUB(NOW(), INTERVAL 100 MINUTE)),
  (2, 1, DATE_SUB(NOW(), INTERVAL 215 MINUTE)),
  (3, 1, DATE_SUB(NOW(), INTERVAL 20 HOUR)),
  (4, 8, DATE_SUB(NOW(), INTERVAL 25 HOUR)),
  (5, 9, DATE_SUB(NOW(), INTERVAL 30 HOUR)),
  (9, 12, DATE_SUB(NOW(), INTERVAL 70 HOUR)),
  (10, 3, DATE_SUB(NOW(), INTERVAL 74 HOUR)),
  (12, 1, DATE_SUB(NOW(), INTERVAL 95 HOUR));

INSERT INTO reposts (post_id, user_id, created_at) VALUES
  (1, 3, DATE_SUB(NOW(), INTERVAL 90 MINUTE)),
  (2, 1, DATE_SUB(NOW(), INTERVAL 205 MINUTE)),
  (3, 8, DATE_SUB(NOW(), INTERVAL 19 HOUR)),
  (4, 10, DATE_SUB(NOW(), INTERVAL 26 HOUR)),
  (7, 4, DATE_SUB(NOW(), INTERVAL 40 HOUR)),
  (9, 6, DATE_SUB(NOW(), INTERVAL 71 HOUR)),
  (12, 3, DATE_SUB(NOW(), INTERVAL 96 HOUR));

INSERT INTO hidden_posts (post_id, user_id, created_at) VALUES
  (8, 2, DATE_SUB(NOW(), INTERVAL 20 HOUR)),
  (15, 11, DATE_SUB(NOW(), INTERVAL 3 DAY));

INSERT INTO post_views (post_id, viewer_id, viewed_at) VALUES
  (1, 2, DATE_SUB(NOW(), INTERVAL 118 MINUTE)),
  (1, 3, DATE_SUB(NOW(), INTERVAL 116 MINUTE)),
  (1, 4, DATE_SUB(NOW(), INTERVAL 114 MINUTE)),
  (1, 5, DATE_SUB(NOW(), INTERVAL 112 MINUTE)),
  (1, 6, DATE_SUB(NOW(), INTERVAL 110 MINUTE)),
  (1, 7, DATE_SUB(NOW(), INTERVAL 108 MINUTE)),
  (2, 1, DATE_SUB(NOW(), INTERVAL 240 MINUTE)),
  (2, 3, DATE_SUB(NOW(), INTERVAL 238 MINUTE)),
  (2, 11, DATE_SUB(NOW(), INTERVAL 226 MINUTE)),
  (2, 12, DATE_SUB(NOW(), INTERVAL 212 MINUTE)),
  (3, 1, DATE_SUB(NOW(), INTERVAL 24 HOUR)),
  (3, 2, DATE_SUB(NOW(), INTERVAL 23 HOUR)),
  (3, 4, DATE_SUB(NOW(), INTERVAL 22 HOUR)),
  (3, 8, DATE_SUB(NOW(), INTERVAL 21 HOUR)),
  (3, 10, DATE_SUB(NOW(), INTERVAL 20 HOUR)),
  (4, 1, DATE_SUB(NOW(), INTERVAL 29 HOUR)),
  (4, 2, DATE_SUB(NOW(), INTERVAL 28 HOUR)),
  (4, 8, DATE_SUB(NOW(), INTERVAL 27 HOUR)),
  (4, 10, DATE_SUB(NOW(), INTERVAL 26 HOUR)),
  (5, 1, DATE_SUB(NOW(), INTERVAL 33 HOUR)),
  (5, 4, DATE_SUB(NOW(), INTERVAL 32 HOUR)),
  (5, 9, DATE_SUB(NOW(), INTERVAL 31 HOUR)),
  (5, 12, DATE_SUB(NOW(), INTERVAL 30 HOUR)),
  (6, 3, DATE_SUB(NOW(), INTERVAL 36 HOUR)),
  (6, 5, DATE_SUB(NOW(), INTERVAL 35 HOUR)),
  (6, 7, DATE_SUB(NOW(), INTERVAL 34 HOUR)),
  (7, 1, DATE_SUB(NOW(), INTERVAL 43 HOUR)),
  (7, 4, DATE_SUB(NOW(), INTERVAL 42 HOUR)),
  (7, 5, DATE_SUB(NOW(), INTERVAL 41 HOUR)),
  (7, 9, DATE_SUB(NOW(), INTERVAL 40 HOUR)),
  (8, 2, DATE_SUB(NOW(), INTERVAL 49 HOUR)),
  (8, 5, DATE_SUB(NOW(), INTERVAL 48 HOUR)),
  (8, 10, DATE_SUB(NOW(), INTERVAL 47 HOUR)),
  (9, 3, DATE_SUB(NOW(), INTERVAL 73 HOUR)),
  (9, 6, DATE_SUB(NOW(), INTERVAL 72 HOUR)),
  (9, 12, DATE_SUB(NOW(), INTERVAL 71 HOUR)),
  (10, 1, DATE_SUB(NOW(), INTERVAL 76 HOUR)),
  (10, 3, DATE_SUB(NOW(), INTERVAL 75 HOUR)),
  (10, 8, DATE_SUB(NOW(), INTERVAL 74 HOUR)),
  (10, 12, DATE_SUB(NOW(), INTERVAL 73 HOUR)),
  (11, 1, DATE_SUB(NOW(), INTERVAL 98 HOUR)),
  (11, 2, DATE_SUB(NOW(), INTERVAL 97 HOUR)),
  (11, 12, DATE_SUB(NOW(), INTERVAL 96 HOUR)),
  (12, 1, DATE_SUB(NOW(), INTERVAL 100 HOUR)),
  (12, 3, DATE_SUB(NOW(), INTERVAL 99 HOUR)),
  (12, 9, DATE_SUB(NOW(), INTERVAL 98 HOUR)),
  (12, 10, DATE_SUB(NOW(), INTERVAL 97 HOUR)),
  (13, 4, DATE_SUB(NOW(), INTERVAL 120 HOUR)),
  (13, 5, DATE_SUB(NOW(), INTERVAL 119 HOUR)),
  (13, 6, DATE_SUB(NOW(), INTERVAL 118 HOUR)),
  (14, 1, DATE_SUB(NOW(), INTERVAL 122 HOUR)),
  (14, 4, DATE_SUB(NOW(), INTERVAL 121 HOUR)),
  (14, 8, DATE_SUB(NOW(), INTERVAL 120 HOUR)),
  (15, 1, DATE_SUB(NOW(), INTERVAL 145 HOUR)),
  (15, 4, DATE_SUB(NOW(), INTERVAL 144 HOUR)),
  (15, 8, DATE_SUB(NOW(), INTERVAL 143 HOUR)),
  (16, 1, DATE_SUB(NOW(), INTERVAL 146 HOUR)),
  (16, 2, DATE_SUB(NOW(), INTERVAL 145 HOUR)),
  (16, 3, DATE_SUB(NOW(), INTERVAL 144 HOUR)),
  (17, 4, DATE_SUB(NOW(), INTERVAL 168 HOUR)),
  (17, 7, DATE_SUB(NOW(), INTERVAL 167 HOUR));

INSERT INTO reports (post_id, reporter_id, category, details, status, resolved_by, resolved_at, created_at) VALUES
  (2, 8, 'other', '评论区已经吵起来了，麻烦看下。', 'OPEN', NULL, NULL, DATE_SUB(NOW(), INTERVAL 50 MINUTE)),
  (6, 9, 'spam', '内容和群公告重复刷屏了。', 'OPEN', NULL, NULL, DATE_SUB(NOW(), INTERVAL 18 HOUR)),
  (17, 4, 'misinformation', '这条活动时间已经变更了。', 'RESOLVED', 1, DATE_SUB(NOW(), INTERVAL 6 DAY), DATE_SUB(NOW(), INTERVAL 7 DAY));

INSERT INTO notifications (type, recipient_id, actor_id, post_id, message, action_label, action_url, is_read, created_at) VALUES
  ('like', 1, 2, 1, NULL, NULL, NULL, FALSE, DATE_SUB(NOW(), INTERVAL 50 MINUTE)),
  ('comment', 1, 3, 1, NULL, NULL, NULL, FALSE, DATE_SUB(NOW(), INTERVAL 44 MINUTE)),
  ('follow', 1, 4, NULL, NULL, NULL, NULL, FALSE, DATE_SUB(NOW(), INTERVAL 16 DAY)),
  ('mention', 12, 3, 3, NULL, NULL, NULL, FALSE, DATE_SUB(NOW(), INTERVAL 19 HOUR)),
  ('system', 7, 1, 17, '你的帖子已被管理员撤回。原因：活动时间已更新，请重新确认后再发布。', '重新编辑', '/compose?edit=17', FALSE, DATE_SUB(NOW(), INTERVAL 6 DAY)),
  ('system', 9, 1, 18, '你的帖子已被管理员删除。原因：涉及不当传播内容。', NULL, NULL, TRUE, DATE_SUB(NOW(), INTERVAL 7 DAY));

INSERT INTO conversations (id, user_a_id, user_b_id, created_at, updated_at) VALUES
  (1, 1, 2, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 20 MINUTE)),
  (2, 1, 3, DATE_SUB(NOW(), INTERVAL 9 DAY), DATE_SUB(NOW(), INTERVAL 2 HOUR)),
  (3, 4, 5, DATE_SUB(NOW(), INTERVAL 6 DAY), DATE_SUB(NOW(), INTERVAL 3 HOUR)),
  (4, 2, 8, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 6 HOUR)),
  (5, 6, 9, DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 8 HOUR)),
  (6, 7, 10, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY));

INSERT INTO messages (conversation_id, sender_id, content, created_at, read_at, recalled, recalled_at) VALUES
  (1, 2, '管理员好，领奖名单里我名字写错了，能帮忙看下吗？', DATE_SUB(NOW(), INTERVAL 40 MINUTE), DATE_SUB(NOW(), INTERVAL 35 MINUTE), FALSE, NULL),
  (1, 1, '可以，把你的正确信息发我，我帮你同步给老师。', DATE_SUB(NOW(), INTERVAL 34 MINUTE), DATE_SUB(NOW(), INTERVAL 30 MINUTE), FALSE, NULL),
  (1, 2, '好的，我已经重新填到表里了。', DATE_SUB(NOW(), INTERVAL 20 MINUTE), NULL, FALSE, NULL),
  (2, 3, '后台权限那块我已经测通了，你有空再看下通知。', DATE_SUB(NOW(), INTERVAL 3 HOUR), DATE_SUB(NOW(), INTERVAL 170 MINUTE), FALSE, NULL),
  (2, 1, '收到，我晚点把系统通知文案再顺一遍。', DATE_SUB(NOW(), INTERVAL 160 MINUTE), DATE_SUB(NOW(), INTERVAL 150 MINUTE), FALSE, NULL),
  (2, 3, '好，那我先去补帖子详情页。', DATE_SUB(NOW(), INTERVAL 140 MINUTE), DATE_SUB(NOW(), INTERVAL 120 MINUTE), TRUE, DATE_SUB(NOW(), INTERVAL 100 MINUTE)),
  (3, 4, '社团海报改好了，你觉得还要不要再加 sponsor？', DATE_SUB(NOW(), INTERVAL 4 HOUR), DATE_SUB(NOW(), INTERVAL 3 HOUR), FALSE, NULL),
  (3, 5, '我觉得不用了，现在已经够清楚了。', DATE_SUB(NOW(), INTERVAL 3 HOUR), DATE_SUB(NOW(), INTERVAL 2 HOUR), FALSE, NULL),
  (3, 4, '行，那我今晚就发。', DATE_SUB(NOW(), INTERVAL 170 MINUTE), NULL, FALSE, NULL),
  (4, 2, '你好，我想问下你之前说的那个空会议室在哪一层？', DATE_SUB(NOW(), INTERVAL 6 HOUR), NULL, FALSE, NULL),
  (5, 6, 'Linux 课设真的过了？我还在改最后一个 bug。', DATE_SUB(NOW(), INTERVAL 9 HOUR), DATE_SUB(NOW(), INTERVAL 8 HOUR), FALSE, NULL),
  (5, 9, '过了，我晚点把我踩过的坑发你。', DATE_SUB(NOW(), INTERVAL 8 HOUR), NULL, FALSE, NULL),
  (6, 7, '你们招新海报能不能把时间再确认一下。', DATE_SUB(NOW(), INTERVAL 28 HOUR), DATE_SUB(NOW(), INTERVAL 27 HOUR), FALSE, NULL),
  (6, 10, '可以，我晚点和负责人再核对。', DATE_SUB(NOW(), INTERVAL 26 HOUR), NULL, FALSE, NULL);

INSERT INTO blocks (blocker_id, blocked_id, created_at) VALUES
  (10, 7, DATE_SUB(NOW(), INTERVAL 20 HOUR));

UPDATE conversations c
SET updated_at = COALESCE(
  (SELECT MAX(m.created_at) FROM messages m WHERE m.conversation_id = c.id),
  c.created_at
);

UPDATE posts p
SET likes_count = (SELECT COUNT(*) FROM post_likes pl WHERE pl.post_id = p.id),
    reposts_count = (SELECT COUNT(*) FROM reposts r WHERE r.post_id = p.id),
    comments_count = (SELECT COUNT(*) FROM comments c WHERE c.post_id = p.id),
    views_count = (SELECT COUNT(*) FROM post_views pv WHERE pv.post_id = p.id);
