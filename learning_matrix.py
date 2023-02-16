import arff
from difference_functions import FeatureCompareFunction


class LearningMatrix:
    # internal constants
    __OBJ_FEATURE = 0
    __OBJ_CLASS = 1
    __MISSING_CHAR = ['?', '*', '-']

    def __init__(self):
        self.feature_dict = {}
        self.obj_list = []
        self.feature_list = []
        self.class_features_list = []

        # similarity functions
        self.feature_cmp_func = FeatureCompareFunction()

    def from_arff(self, path):
        arff_data = arff.load(open(path, 'r'))
        class_att = arff_data['attributes'][-1]

        for att in arff_data['attributes']:
            if att != class_att:
                self.add_feature(att[0], att[1])
            else:
                self.add_feature(att[0], att[1], is_class=True)

        for obj in arff_data['data']:
            self.add_obj(obj, class_atts=len(self.class_features_list))

    def add_feature(self, name, domain, is_class=False, default=None, cmp_fun='eq'):
        """
        Add feature to learning matrix. If default value not defined, in values set will take the first value, in
         continuous domains will be 0. For existent objects that not contains the attribute, it will be added and
         set the default value.
        Args:
            name: feature name
            domain: feature type
            is_class: is a class feature
            default: feature default value
            cmp_fun: feature comparison function

        Returns: None

        """
        # adding new feature
        if name not in self.feature_dict.keys():
            if is_class:
                # add name to reference list
                self.class_features_list.append(name)
            else:
                # add name to reference list
                self.feature_list.append(name)

            if default is None:
                if domain == 'REAL' or domain == 'real' or domain == 'INTEGER' or domain == 'integer':
                    default = 0
                else:
                    default = domain[0]
            self.feature_dict[name] = {'dom': domain, 'is_class': is_class, 'default': default}
            # set cmp function
            self.feature_cmp_func.set_feature_func(self.feature_dict[name], cmp_fun)

        # updating objects
        if len(self.obj_list) > 0:
            attribute_group = self.__OBJ_FEATURE
            if is_class:
                attribute_group = self.__OBJ_CLASS

            for obj in self.obj_list:
                obj[attribute_group].append(self.feature_dict[name]['default'])

    def add_obj(self, new_obj, class_atts=1):
        """
        Add new object to obj list, assuming as class attributes the last <class_atts> elements in new_obj,
         by default 1, but for unknow class values may be 0.
        Args:
            new_obj: object  vector
            class_atts: amount of class elements

        Returns: None

        """
        size = len(new_obj)
        self.obj_list.append((new_obj[0:size - class_atts], new_obj[size - class_atts:size]))

    def __get_obj_diff_vec(self, obj_1, obj_2):
        result = []
        check_value = 0
        self.ones_count = 0
        for val in range(0, len(self.feature_list)):
            curr_val = 0 if obj_1[val] == obj_2[val] else 1
            check_value += curr_val
            result.append(curr_val)
            self.ones_count += curr_val
        if check_value == 0:
            raise Exception("Inconsistent dataset")
        return result

    def get_basic_matrix(self):
        """
        Returns: Basic matrix

        """
        MB = []  # Basic matrix
        for obj_a_pos in range(0, len(self.obj_list)):
            for obj_b_pos in range(obj_a_pos + 1, len(self.obj_list)):
                if self.obj_list[obj_a_pos][self.__OBJ_CLASS] != self.obj_list[obj_b_pos][self.__OBJ_CLASS]:
                    add_to_mb = True
                    dif_vector = (self.__get_obj_diff_vec(self.obj_list[obj_a_pos][self.__OBJ_FEATURE],
                                                          self.obj_list[obj_b_pos][self.__OBJ_FEATURE]))
                    rm_rows = []
                    for mb_row_pos in range(0, len(MB)):
                        status = self.__get_cmp_status(dif_vector, MB[mb_row_pos])
                        if status == "SUB":
                            rm_rows.append(mb_row_pos)
                        if status == "SUP":
                            add_to_mb = False
                            break

                    for mb_row_pos in range(len(rm_rows) - 1, -1, -1):
                        del MB[rm_rows[mb_row_pos]]

                    if len(MB) == 0 or add_to_mb:
                        MB.append(dif_vector)

        return MB

    def __get_cmp_status(self, vector_a, vector_b):
        i = 0
        a_comp = False
        b_comp = False
        a_count = 0
        b_count = 0
        while i < len(vector_a):
            if vector_a[i] < vector_b[i]:
                b_count += 1
                b_comp = True
            if vector_a[i] > vector_b[i]:
                a_count += 1
                a_comp = True
            i += 1
            if a_comp and b_comp:
                return "COMP"

        if a_count > b_count:
            return "SUP"
        if b_count > a_count:
            return "SUB"
        return "SUB"
