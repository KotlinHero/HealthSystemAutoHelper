/* 任务台设计稿 · 极简状态机（Alpine） */
window.app = function () {
  const ALLOWED = ['idle', 'drawer', 'running', 'done', 'failed', 'settings'];
  const qs = new URLSearchParams(location.search).get('state');
  const scene = ALLOWED.includes(qs) ? qs : 'idle';

  return {
    scene: scene,
    showPw: false,
    openKey: 'hyper-visit',

    groups: [
      {
        key: 'hypertension', name: '高血压患者管理', color: '#D93A3A', tint: '#FBECEA',
        tasks: [
          { key: 'hyper-visit', title: '导入高血压随访', icon: 'i-activity',
            desc: '把随访记录按行录入，逐条保存', meta: '上次 3 月 12 日 · 131 条', statusText: '3 条未录入',
            fail: true, nowHint: '随访表' },
          { key: 'hyper-group', title: '导入高血压分组评估', icon: 'i-layers',
            desc: '按风险分层结果更新患者分组', meta: '上次 2 月 27 日 · 96 条', statusText: '全部成功',
            fail: false, nowHint: '分组评估页' }
        ]
      },
      {
        key: 'diabetes', name: '糖尿病患者管理', color: '#D98324', tint: '#FBF0DE',
        tasks: [
          { key: 'diabetes-visit', title: '导入糖尿病随访', icon: 'i-droplet',
            desc: '把随访记录按行录入，逐条保存', meta: '上次 3 月 10 日 · 88 条', statusText: '全部成功',
            fail: false, nowHint: '随访表' },
          { key: 'diabetes-group', title: '导入糖尿病分组评估', icon: 'i-layers',
            desc: '按风险分层结果更新患者分组', meta: '上次 1 月 15 日 · 74 条', statusText: '2 条未录入',
            fail: true, nowHint: '分组评估页' }
        ]
      },
      {
        key: 'exam', name: '健康体检表', color: '#1F8A4C', tint: '#E8F4EC',
        tasks: [
          { key: 'exam-new', title: '新建健康体检表', icon: 'i-clipboard-plus',
            desc: '给还没有体检档案的居民建表', meta: '上次 3 月 8 日 · 45 条', statusText: '全部成功',
            fail: false, nowHint: '体检表' },
          { key: 'exam-fill', title: '完善健康体检表', icon: 'i-clipboard-check',
            desc: '补齐已有体检表里缺失的项', meta: '上次 2 月 20 日 · 120 条', statusText: '全部成功',
            fail: false, nowHint: '体检表' }
        ]
      },
      {
        key: 'contract', name: '签约服务', color: '#5B5BD6', tint: '#EDF0FD',
        tasks: [
          { key: 'contract-import', title: '导入签约服务', icon: 'i-pen',
            desc: '按名单批量建立签约关系', meta: '上次 3 月 5 日 · 62 条', statusText: '全部成功',
            fail: false, nowHint: '签约页' },
          { key: 'archive-save', title: '档案保存', icon: 'i-archive',
            desc: '把编辑中的居民档案逐个保存', meta: '上次 3 月 5 日 · 62 条', statusText: '1 条未录入',
            fail: true, nowHint: '档案页' }
        ]
      }
    ],

    get view() { return this.scene === 'settings' ? 'settings' : 'board'; },
    get expandKey() {
      return ['running', 'done', 'failed'].includes(this.scene) ? this.openKey : null;
    },
    get drawerOpen() { return this.scene === 'drawer'; },
    get activeTask() {
      const all = this.groups.flatMap(g => g.tasks.map(t => Object.assign({}, t, { color: g.color, tint: g.tint })));
      return all.find(t => t.key === this.openKey) || all[0];
    },

    openDrawer(key) { this.openKey = key; this.scene = 'drawer'; },
    closeDrawer() { this.scene = 'idle'; },
    start() { this.scene = 'running'; },
    goBoard() { if (this.scene === 'settings') this.scene = 'idle'; },
    goSettings() { this.scene = 'settings'; }
  };
};
