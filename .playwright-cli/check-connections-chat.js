async (page) => {
  await page.goto('http://127.0.0.1:5173/connections');
  await page.getByRole('button', { name: '聊天' }).first().click();
  await page.waitForURL(/\/chat\?conversation=/);
  const url = page.url();
  const bodyText = await page.textContent('body');
  return { url, hasChatHeader: (bodyText || '').includes('聊天'), hasConversation: (bodyText || '').includes('@bob') || (bodyText || '').includes('@alice') || (bodyText || '').includes('@janedoe') || (bodyText || '').includes('@johndoe') };
}
