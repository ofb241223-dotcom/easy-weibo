async (page) => {
  await page.goto('http://127.0.0.1:5173/chat?conversation=1');
  await page.waitForTimeout(1000);
  const textArea = page.locator('textarea').first();
  await textArea.fill('测试😀附件');
  const fileInput = page.locator('input[type="file"]').first();
  await fileInput.setInputFiles('.playwright-cli/chat-image.png');
  await page.waitForTimeout(1000);
  const chatText = await page.textContent('body');

  await page.goto('http://127.0.0.1:5173/post/1');
  await page.waitForTimeout(600);
  await page.getByPlaceholder('发布你的回复').fill('评论😀带图');
  const commentFile = page.locator('main input[type="file"]').first();
  await commentFile.setInputFiles('.playwright-cli/chat-image.png');
  await page.getByRole('button', { name: '回复' }).click();
  await page.waitForTimeout(1500);
  const commentBody = await page.textContent('body');
  return {
    chatHasAttachment: (chatText || '').includes('chat-image.png'),
    commentCreated: (commentBody || '').includes('评论😀带图')
  };
}
