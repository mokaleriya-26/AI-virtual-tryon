import os
import re

def remove_slf4j(file_path):
    with open(file_path, 'r') as f:
        content = f.read()

    if '@Slf4j' in content:
        # Import logger
        if 'import org.slf4j.Logger;' not in content:
            content = content.replace('import lombok.extern.slf4j.Slf4j;', 'import org.slf4j.Logger;\nimport org.slf4j.LoggerFactory;')
            content = content.replace('import lombok.extern.slf4j.Slf4j;\n', '')
            content = content.replace('@Slf4j\n', '')
            
            # Find class name
            class_match = re.search(r'public class (\w+)', content)
            if class_match:
                class_name = class_match.group(1)
                logger_declaration = f'\n    private static final Logger log = LoggerFactory.getLogger({class_name}.class);\n'
                
                # Insert logger after class declaration
                content = content.replace(f'public class {class_name} {{', f'public class {class_name} {{{logger_declaration}')

        with open(file_path, 'w') as f:
            f.write(content)
        print(f'Processed SLF4J in {file_path}')

# Files using @Slf4j
slf4j_files = [
    'src/main/java/com/virtualfit/ai/controller/StyleFitController.java',
    'src/main/java/com/virtualfit/ai/service/HuggingFaceService.java',
    'src/main/java/com/virtualfit/ai/service/StyleFitService.java',
    'src/main/java/com/virtualfit/ai/exception/GlobalExceptionHandler.java'
]

for file in slf4j_files:
    remove_slf4j(file)
