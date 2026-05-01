import translations from "@/views/qf/sfc_private/flowable-designer/ZhCn";

function customTranslate(template: string, replacements?: Record<string, string>): string {
  const translated = translations[template] || template;
  if (!replacements) {
    return translated;
  }
  return translated.replace(/{([^}]+)}/g, (_: string, key: string) => {
    const val = replacements[key];
    if (val === undefined) {
      return `{${key}}`;
    }
    return translations[val] || val;
  });
}

const translateModule = {
  translate: ["value", customTranslate],
};

export default translateModule;
