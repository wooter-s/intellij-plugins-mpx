// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
export function isVueFile(
  fileName: string,
): boolean {
  const extension = fileName
    .slice(-4)
    .toLowerCase()

  return extension === ".mpx"
}
