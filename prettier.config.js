module.exports = {
  // Base rules from package.json: "prettier": "@ionic/prettier-config"
  ...require('@ionic/prettier-config'),

  // Ensure the Java plugin is loaded for both CLI and VS Code
  plugins: ['prettier-plugin-java'],

  // Make sure .java files use the Java parser
  overrides: [
    {
      files: ['**/*.java', '*.java'],
      options: { parser: 'java' },
    },
  ],
};
