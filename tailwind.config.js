/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    './src/main/webapp/**/*.jsp',
    './src/main/webapp/**/*.js',
    './src/main/webapp/**/*.css'
  ],
  corePlugins: {
    preflight: false
  },
  theme: {
    extend: {
      colors: {
        accent: {
          50: '#eef2ff',
          100: '#e0e7ff',
          200: '#c7d2fe',
          300: '#a5b4fc',
          400: '#818cf8',
          500: '#6366f1',
          600: '#4f46e5',
          700: '#4338ca',
          800: '#3730a3',
          900: '#312e81',
        },
        surface: {
          0: '#fafafa',
          1: '#ffffff',
          2: '#f4f4f5',
          3: '#e4e4e7',
        }
      },
      fontFamily: {
        'outfit': ['Outfit', 'sans-serif'],
        'mono': ['JetBrains Mono', 'monospace']
      }
    }
  }
}
