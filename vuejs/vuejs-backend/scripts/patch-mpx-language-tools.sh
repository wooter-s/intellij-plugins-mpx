#!/usr/bin/env bash

set -euo pipefail

script_dir=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
backend_dir=$(cd -- "$script_dir/.." && pwd)

ws_source="$backend_dir/src-js/ws-typescript-vue-plugin/src/vue.ts"
ws_bundle="$backend_dir/gen-resources/vue-service/node_modules/ws-typescript-vue-plugin/index.js"

perl -pi -e 's/extension === "\x2evue"/extension === ".mpx"/g' "$ws_source"

if [[ -f "$ws_bundle" ]]; then
  perl -pi -e 's/==="\x2evue"/===".mpx"/g' "$ws_bundle"
fi

shopt -s nullglob
language_server_bundles=("$backend_dir"/vue-language-tools/language-server/*/bin/vue-language-server.js)
typescript_plugin_bundles=("$backend_dir"/vue-language-tools/typescript-plugin/*/node_modules/@vue/typescript-plugin/dist/index.js)

for bundle in "${language_server_bundles[@]}"; do
  perl -pi -e 's/extensions: \["\x2evue"\]/extensions: [".mpx"]/g' "$bundle"
done

for bundle in "${typescript_plugin_bundles[@]}"; do
  perl -pi -e 's/extensions:\["\x2evue"\]/extensions:[".mpx"]/g' "$bundle"
done

grep -Fq 'extension === ".mpx"' "$ws_source"
for bundle in "${language_server_bundles[@]}"; do
  grep -Fq 'extensions: [".mpx"]' "$bundle"
done
for bundle in "${typescript_plugin_bundles[@]}"; do
  grep -Fq 'extensions:[".mpx"]' "$bundle"
done
